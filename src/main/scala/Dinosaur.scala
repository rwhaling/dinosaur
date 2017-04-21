package io.dinosaur
import scalanative.native._
import io.dinosaur.CgiUtils._

case class Request(
  env:Map[String, String],
  scriptName:String,
  pathInfo:Seq[String],
  queryParameters:Map[String, Seq[String]]
)

case class Response(
  statusCode: Int,
  body: String,
  headers: Map[String, String] = Map("Content-type" -> "text/html; charset=utf-8")
)

case class Handler(
  pattern: Seq[String],
  handler: Request => Response
) {
  def this(pattern:String, handler: Request => Response) = {
    this(CgiUtils.parsePathInfo(pattern),handler)
  }
}

case class Router(handlers:Seq[Handler]) {
  def handle(path:String, f: Request => Response):Router = {
    val new_handler = Handler(CgiUtils.parsePathInfo(path), f)
    return Router(Seq(new_handler) ++ this.handlers)
  }

  def handle(path:String, statusCode: Int, f: Request => String):Router = {
    val decorated = { r:Request => Response(statusCode,f(r)) }
    val new_handler = Handler(CgiUtils.parsePathInfo(path), decorated)
    return Router(Seq(new_handler) ++ this.handlers)
  }


  def dispatch():Response = {
    val request = Router.parseRequest()
    val matches = for ( h @ Handler(pattern, handler) <- this.handlers
                        if request.pathInfo.startsWith(pattern)) yield h
    val bestHandler = matches.maxBy( _.pattern.size )
    val response = bestHandler.handler(request)
    for ( (k,v) <- response.headers) {
      System.out.println(s"${k}: ${v}")
    }
    System.out.println()
    System.out.println(response.body)
    return response
  }
}

object Router {
  def parseRequest():Request = {
    val scriptName = env("SCRIPT_NAME")
    val pathInfo = parsePathInfo(env("PATH_INFO"))
    val queryString = parseQueryString(env("QUERY_STRING"))
    val environ:Map[String,String] = Map()
    val request = Request(environ, scriptName, pathInfo, queryString)
    request
  }

  def setup():Router = {
    val errorResponse = Response(404, "No path matched the request")
    val errorHandler = Handler(List(), (_) => errorResponse)

    val debugHandler = Handler(List("debug"), (request) => {
      Response(200, request.toString)
    })

    val handlers:Seq[Handler] = List(debugHandler, errorHandler)
    Router(handlers)
  }
}
