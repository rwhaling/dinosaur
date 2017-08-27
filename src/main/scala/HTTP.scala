package io.dinosaur
import scala.language.implicitConversions
import scalanative.native._

package object dinosaur {}

sealed trait Method
case object GET extends Method
case object POST extends Method
case object PUT extends Method
case object DELETE extends Method
case object HEAD extends Method
case object OPTIONS extends Method
case object CONNECT extends Method
case object PATCH extends Method

case class Request(
  method: Method,
  pathInfo: Seq[String],
  params: Map[String, Seq[String]],
  env: CString => String = CgiUtils.env
)

case class Response(
  body: ResponseBody,
  statusCode: Int = 200,
  headers: Map[String, String] = Map("Content-type" -> "text/html; charset=utf-8")
) {
  def bodyToString() = this.body match {
    case StringBody(body) => body
  }
  def inferHeaders(): Map[String, String] = {
    if (this.headers.contains("Content-type")) return this.headers
    else {
      val inferredContentType = this.body match {
        case StringBody(_) => "text/html; charset=utf-8"
      }
      val inferredHeaders = this.headers + ("Content-type" -> inferredContentType)
      return inferredHeaders
    }
  }
}

sealed trait ResponseBody
case class StringBody(body:String) extends ResponseBody
// TODO: HTML and JSON.  Protobuf, Avro, Thrift?

object Response {
  implicit def stringToResponse(body:String):Response = Response(StringBody(body))
  implicit def stringToRequestResponse(body:String):(Request => Response) = _ => Response(StringBody(body))
}
