package io.dinosaur
import scalanative.native._

sealed trait RequestType
case object FCGI_BEGIN_REQUEST extends RequestType
case object FCGI_ABORT_REQUEST extends RequestType
case object FCGI_END_REQUEST extends RequestType
case object FCGI_PARAMS extends RequestType
case object FCGI_STDIN extends RequestType
case object FCGI_STDOUT extends RequestType
case object FCGI_STDERR extends RequestType
case object FCGI_DATA extends RequestType
case object FCGI_GET_VALUES extends RequestType
case object FCGI_GET_VALUES_RESULT extends RequestType
case object FCGI_UNKNOWN_TYPE extends RequestType

case class RecordHeader(version:Int, rec_type:RequestType, reqId:Int, length:Int, padding:Int)

object FastCGIUtils {
  def readHeader(input: Ptr[Byte], offset:Long): RecordHeader = {
    val version = input(0 + offset) & 0xFF
    val rec_type = (input(1 + offset) & 0xFF) match {
      case 0 => FCGI_UNKNOWN_TYPE
      case 1 => FCGI_BEGIN_REQUEST
      case 2 => FCGI_ABORT_REQUEST
      case 3 => FCGI_END_REQUEST
      case 4 => FCGI_PARAMS
      case 5 => FCGI_STDIN
      case 6 => FCGI_STDOUT
      case 7 => FCGI_STDERR
      case 8 => FCGI_DATA
      case 9 => FCGI_GET_VALUES
      case 10 => FCGI_GET_VALUES_RESULT
      case _ => FCGI_UNKNOWN_TYPE
    }
    val req_id_b1 = (input(2 + offset) & 0xFF)
    val req_id_b0 = (input(3 + offset) & 0xFF)
    val req_id = (req_id_b1 << 8) + (req_id_b0 & 0xFF)
    // System.err.println("req_id_b1: $req_id_b1  -- req_id_b0: $req_id_b0  -- req_id: $req_id")
    // println(s"length bytes: ${input(4 + offset) & 0xFF} ${input(5 + offset) & 0xFF}")
    val length = ((input(4 + offset) & 0xFF) << 8) + (input(5 + offset) & 0xFF)
    val padding = input(6 + offset) & 0xFF
    RecordHeader(version,rec_type,req_id,length,padding)
  }

  def readParams(byteArray: Ptr[Byte], arr_offset:Long, length:Long): Seq[(String,String)] = {
    var offset = arr_offset
    var results:Seq[(String,String)] = Seq()

    while (offset < (arr_offset + length)) {
      val name_length = if ((byteArray(offset) & 0x80) == 0) {
        byteArray(offset)
      } else {
        ((byteArray(offset) & 0x7F) << 24) +
        ((byteArray(offset + 1) & 0xFF) << 16) +
        ((byteArray(offset + 2) & 0xFF) << 8) +
        (byteArray(offset + 3) & 0xFF)
      }
      if (name_length <= 127) offset += 1 else offset += 4

      val value_length = if ((byteArray(offset) & 0x80) == 0) {
        byteArray(offset)
      } else {
        ((byteArray(offset) & 0x7F) << 24) +
        ((byteArray(offset + 1) & 0xFF) << 16) +
        ((byteArray(offset + 2) & 0xFF) << 8) +
        (byteArray(offset + 3) & 0xFF)
      }
      if (value_length <= 127) offset += 1 else offset += 4

      val name_array:CString = stackalloc[CChar](name_length + 1)
      string.memset(name_array, 0, name_length + 1)
      string.memcpy(name_array, byteArray + offset, name_length)
      val name = name_array.cast[CString]

      offset += name_length

      val value_array = stackalloc[CChar](value_length + 1)
      string.memset(value_array, 0, value_length + 1)
      string.memcpy(value_array,byteArray + offset, value_length)
      val value = value_array.cast[CString]

      offset += value_length
      // Zone { implicit z =>
      //   val n = fromCString(name)
      //   val v = fromCString(value)
      //   // System.err.println(s"$name_length $value_length :: $n : $v @ $offset")
      //   results = results :+ (n,v)
      // }
    }
    return results
  }

  def readAllHeaders(input: Ptr[Byte], input_size:Long): Seq[RecordHeader] = {
    var offset = 0
    var res:Vector[RecordHeader] = Vector()
    while (offset < input_size) {
      val header = readHeader(input, offset)

      if (header.rec_type == FCGI_PARAMS) {
        readParams(input, offset + 8, header.length)
      }

      offset += (8 + header.length + header.padding)
      // println(header)
      // println(s"offset $offset")
      res = res :+ header
    }
    res
  }

  def writeResponse(req_id: Int, response: Response): Unit = {
    val req_id_b1 = (req_id & 0xFF00) >> 8
    val req_id_b0 = req_id & 0xFF
    val responseBody = response.body match {
      case StringBody(s) => s
      case _ => ""
    }

    val len = responseBody.size
    val len_b1 = (len & 0xFF00) >> 8
    val len_b0 = len & 0xFF
    val endReqHeader = List(1,3, req_id_b1, req_id_b0 )

    Zone { implicit z =>
      stdio.printf(c"%c%c%c%c%c%c%c%c", 1, 6, req_id_b1, req_id_b0, len_b1, len_b0, 0, 0)
      stdio.printf(c"%s", toCString(responseBody))
      stdio.printf(c"%c%c%c%c%c%c%c%c", 1, 6, req_id_b1, req_id_b0, 0, 0, 0, 0)
      stdio.printf(c"%c%c%c%c%c%c%c%c", 1, 3, req_id_b1, req_id_b0, 0, 8, 0, 0)
      stdio.printf(c"%c%c%c%c%c%c%c%c", 0, 0, 0, 0, 0, 0, 0, 0)

    }
    stdio.fflush(stdio.stdout)
  }
}
