package io.dinosaur
import scalanative.native._

object UVFCGIRouter {
  import LibUV._
  import FastCGIUtils._
  val pipe_size = uv_handle_size(7)
  val loop:Loop = uv_default_loop()
  val write_req_size = uv_req_size(3)
  val shutdown_req_size = uv_req_size(4)
  var closing:PipeHandle = null

  def onConnect(server:PipeHandle, status:Int): Unit = {
    // println("connection received!")

    val client:PipeHandle = stdlib.malloc(pipe_size)
    uv_pipe_init(loop, client, 0)
    var r = uv_accept(server, client)
    // println(s"uv_accept returned $r")
    uv_read_start(client, onAllocCB, onReadCB)
  }
  val onConnectCB = CFunctionPtr.fromFunction2(onConnect)

  def onAlloc(pipe:PipeHandle, size:CSize, buffer:Ptr[Buffer]): Unit = {
    // println(s"allocing 2048 bytes")
    val buf = stdlib.malloc(2048)
    if (buf == null) {
      println("WARNING: malloc failed")
      sys.exit(1)
    }
    !buffer._1 = buf
    !buffer._2 = 2048
  }
  val onAllocCB = CFunctionPtr.fromFunction3(onAlloc)

  def onRead(pipe:PipeHandle, size:CSSize, buffer:Ptr[Buffer]): Unit = {
    // println(s"reading $size bytes")
    if (size >= 0) {
      // println(s"reading")
      // stdio.printf(c"read %d bytes: %.*s", size, size, !buffer._1)
      var position = 0
      // var frameOffsets:Seq[(Int,RecordHeader)] = Seq()
      var params:(Int,RecordHeader) = (0,null)
      var stdin:(Int,RecordHeader) = (0,null)
      var reqId = 1

      while (position < size) {
        val header = readHeader(!buffer._1,position)
        reqId = header.reqId
        // println(position,header)
        // frameOffsets = frameOffsets :+ (position,header)
        if (header.rec_type == FCGI_PARAMS & header.length > 0)
          params = (position,header)
        else if (header.rec_type == FCGI_STDIN & header.length > 0)
          stdin = (position, header)
        position += (8 + header.length + header.padding)
      }
      // println(s"read ${frameOffsets.length} frames ${frameOffsets} in $position bytes")

      // val debug = stdio.fopen(c"debug.in",c"a")
      // val wcount = stdio.fwrite(!buffer._1, 1, position, debug)
      // stdio.fclose(debug)
      // println(s"wrote $wcount bytes to debug.in")

      val write_req:WriteReq = stdlib.malloc(write_req_size).cast[Ptr[Ptr[Byte]]]
      !write_req = !buffer._1
      // stdlib.free(buffer.cast[Ptr[Byte]])

      val resp = c"Content-type: text/html\r\n\r\nhello"

      !buffer._2 = makeResponse(reqId, resp, !write_req)
      uv_write(write_req, pipe, buffer, 1, onWriteCB)
    } else {
      // println("stopping reads on client")
      uv_read_stop(pipe)
      // println(s"mallocing $shutdown_req_size bytes for shutdownReq")
      val shutdownReq = stdlib.malloc(shutdown_req_size).cast[ShutdownReq]
      !shutdownReq = pipe
      // println(s"shutting down handle $pipe via request $shutdownReq")
      // closing = pipe
      uv_shutdown(shutdownReq, pipe, myShutdownCB)
      stdlib.free(!buffer._1)
      // uv_close(pipe, onCloseCB)
      // if (uv_is_closing(pipe)) {
      //   println("pipe already closing")
      // } else {
      //   println("about to call close from onRead")
      //   uv_close(pipe, onCloseCB)
      // }
      // println("uv_close called")
    }
    // println("done with read")
  }
  val onReadCB = CFunctionPtr.fromFunction3(onRead)

  def makeResponse(req_id:Int, resp:CString, buf: Ptr[Byte]): Int = {
    val req_id_b1 = (req_id & 0xFF00) >> 8
    val req_id_b0 = req_id & 0xFF

    val len = string.strlen(resp).toInt
    val len_b1 = (len & 0xFF00) >> 8
    val len_b0 = len & 0xFF

    // Zone { implicit z =>
      stdio.sprintf(buf, c"%c%c%c%c%c%c%c%c", 1, 6, req_id_b1, req_id_b0, len_b1, len_b0, 0, 0)
      stdio.sprintf(buf + 8, c"%s", resp)
      stdio.sprintf(buf + 8  + len, c"%c%c%c%c%c%c%c%c", 1, 6, req_id_b1, req_id_b0, 0, 0, 0, 0)
      stdio.sprintf(buf + 16 + len, c"%c%c%c%c%c%c%c%c", 1, 3, req_id_b1, req_id_b0, 0, 8, 0, 0)
      stdio.sprintf(buf + 24 + len, c"%c%c%c%c%c%c%c%c", 0, 0, 0, 0, 0, 0, 0, 0)
    // }

    // val debug = stdio.fopen(c"debug.out",c"a")
    // val wcount = stdio.fwrite(buf, 1, resp.size + 32, debug)
    // stdio.fclose(debug)
    // println(s"wrote $wcount bytes to debug.out")

    return len + 32
  }

  def onWrite(writeReq:WriteReq, status:Int): Unit = {
    if (status != 0) {
      println(s"write got status $status")
    }
    // println("Wrote succesfully")
    stdio.fflush(stdio.stdout)
    stdlib.free(!writeReq)
  }
  val onWriteCB = CFunctionPtr.fromFunction2(onWrite)

  def myShutdownHandler(shutdownReq:ShutdownReq, status:Int): Unit = {
    // println(s"shutdown completed with status $status")
  //   // val parsed_shutdownReq = shutdownReq.cast[Ptr[CStruct5[Ptr[Byte],Int,Ptr[Byte],Ptr[Byte],Ptr[Byte]]]]
  //   // println("about to close")
  //   // uv_close(!parsed_shutdownReq._5, onCloseCB)
  //   // println("close called")
    // uv_close(!shutdownReq, onCloseCB)
    val pipe:PipeHandle = !shutdownReq
    if (uv_is_closing(pipe) != 0) {
      // println("pipe already closing")
    } else {
      // println("about to call close from myShutdownHandler")
      uv_close(pipe, onCloseCB)
    }

  }
  val myShutdownCB = CFunctionPtr.fromFunction2(myShutdownHandler)

  def onClose(handle:PipeHandle): Unit = {
    // println("onClose called")
    // closing = null
  }
  val onCloseCB = CFunctionPtr.fromFunction1(onClose)
}

case class UVFCGIRouter(handlers:Seq[Handler]) extends Router {
  import LibUV._
  def handle(method: Method, path:String)(f: Request => Response):Router = {
    return UVFCGIRouter(Seq())
  }

  def dispatch(): Unit = {
    println("Hello, libuv world!")
    val loop:Loop = uv_default_loop()
    val pipe_size = uv_handle_size(7)
    val pipe:PipeHandle = stackalloc[Byte](pipe_size)
    uv_pipe_init(loop, pipe, 0)
    var r = uv_pipe_bind(pipe, c"/tmp/app.socket")
    println(s"uv_pipe_bind returned $r")
    def cbf(pipe:PipeHandle, status:Int):Unit = { () }
    r = uv_listen(pipe, 4096, UVFCGIRouter.onConnectCB)

    println(s"uv_listen returned $r")
    r = uv_run(loop, 0)
    println(s"uv_run returned $r")

  }
}

@link("uv")
@extern
object LibUV {
  type PipeHandle = Ptr[Byte]
  type Loop = Ptr[Byte]
  type Buffer = CStruct2[Ptr[Byte],CSize]
  type WriteReq = Ptr[Ptr[Byte]]
  type ShutdownReq = Ptr[Ptr[Byte]]
  type Connection = Ptr[Byte]
  type ConnectionCB = CFunctionPtr2[PipeHandle,Int,Unit]
  type AllocCB = CFunctionPtr3[PipeHandle,CSize,Ptr[Buffer],Unit]
  type ReadCB = CFunctionPtr3[PipeHandle,CSSize,Ptr[Buffer],Unit]
  type WriteCB = CFunctionPtr2[WriteReq,Int,Unit]
  type ShutdownCB = CFunctionPtr2[ShutdownReq,Int,Unit]
  type CloseCB = CFunctionPtr1[PipeHandle,Unit]

  def uv_default_loop(): Loop = extern
  def uv_loop_size(): CSize = extern
  def uv_handle_size(h_type:Int): CSize = extern
  def uv_req_size(r_type:Int): CSize = extern
  def uv_pipe_init(loop:Loop, handle:PipeHandle, ipcFlag:Int ): Unit = extern
  def uv_pipe_bind(handle:PipeHandle, socketName:CString): Int = extern
  def uv_listen(handle:PipeHandle, backlog:Int, callback:ConnectionCB): Int = extern
  def uv_accept(server:PipeHandle, client:PipeHandle): Int = extern
  def uv_read_start(client:PipeHandle, allocCB:AllocCB, readCB:ReadCB): Int = extern
  def uv_write(writeReq:WriteReq, client:PipeHandle, bufs: Ptr[Buffer], numBufs: Int, writeCB:WriteCB): Int = extern
  def uv_read_stop(client:PipeHandle): Int = extern
  def uv_shutdown(shutdownReq:ShutdownReq, client:PipeHandle, shutdownCB:ShutdownCB): Int = extern
  def uv_close(handle:PipeHandle, closeCB: CloseCB): Unit = extern
  def uv_is_closing(handle:PipeHandle): Int = extern
  def uv_run(loop:Loop, runMode:Int): Int = extern
}

/*
struct sockaddr_in

uv_loop_t
uv_write_t
uv_tcp_t
uv_stream_t
uv_buf_t
uv_handle_t

uv_default_loop
uv_tcp_init
uv_ip4_addr
uv_tcp_bind
uv_listen
uv_run
uv_tcp_init
uv_accept
uv_read_start
uv_write
uv_strerror
uv_close
*/
