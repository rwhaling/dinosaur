package io.dinosaur
import scalanative.native._
import io.dinosaur.CgiUtils._

case class Handler(
  method : Method,
  pattern: Seq[String],
  handler: Request => Response
) {
  def this(method: Method, pattern:String, handler: Request => Response) = {
    this(method, CgiUtils.parsePathInfo(pattern),handler)
  }
}

case class Router(handlers:Seq[Handler]) {
  def handle(method: Method, path:String)(f: Request => Response):Router = {
    val new_handler = Handler(method, CgiUtils.parsePathInfo(path), f)
    return Router(Seq(new_handler) ++ this.handlers)
  }

  def get(path:String)(f: Request => Response):Router = handle(GET, path)(f)
  def post(path:String)(f: Request => Response):Router = handle(POST, path)(f)
  def put(path:String)(f: Request => Response):Router = handle(PUT, path)(f)
  def delete(path:String)(f: Request => Response):Router = handle(DELETE, path)(f)

  def dispatch():Response = {
    val request = Router.parseRequest()
    val matches = for ( h @ Handler(method, pattern, handler) <- this.handlers
                        if request.method == method
                        if request.pathInfo.startsWith(pattern)) yield h
    val bestHandler = matches.maxBy( _.pattern.size )
    val response = bestHandler.handler(request)
    for ( (k,v) <- response.inferHeaders ) {
      System.out.println("%s: %s".format(k,v))
    }
    System.out.println()
    System.out.println(response.bodyToString)
    return response
  }
}

object Router {
  def parseRequest():Request = {
    val scriptName = env("SCRIPT_NAME")
    val pathInfo = parsePathInfo(env("PATH_INFO"))
    val queryString = parseQueryString(env("QUERY_STRING"))
    val method = env("METHOD") match {
      case "GET"    => GET
      case "POST"   => POST
      case "PUT"    => PUT
      case "DELETE" => DELETE
      case "HEAD"   => HEAD
      case "OPTIONS"=> OPTIONS
      case "PATCH"  => PATCH
      case _        => GET
    }
    val environ:Map[String,String] = Map()
    val request = Request(method, pathInfo, queryString)
    request
  }

  def init():Router = {
    val errorResponse = Response(StringBody("No path matched the request"))
    val errorHandler = Handler(GET,List(), (_) => errorResponse)

    val debugHandler = Handler(GET,List("debug"), (request) => {
      Response(StringBody(request.toString))
    })

    val handlers:Seq[Handler] = List(debugHandler, errorHandler)
    Router(handlers)
  }
}
