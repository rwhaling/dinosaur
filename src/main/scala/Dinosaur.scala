package io.dinosaur
import scalanative.native._
import io.dinosaur.CgiUtils._
import io.dinosaur.FastCGIUtils._

sealed trait RouterMode
case object CGIMode extends RouterMode
case object FCGIMode extends RouterMode

case class Handler(
  method : Method,
  pattern: Seq[String],
  handler: Request => Response
) {
  def this(method: Method, pattern:String, handler: Request => Response) = {
    this(method, CgiUtils.parsePathInfo(pattern),handler)
  }
}

trait Router {
  def handle(method: Method, path:String)(f: Request => Response):Router
  def get(path:String)(f: Request => Response):Router = handle(GET, path)(f)
  def post(path:String)(f: Request => Response):Router = handle(POST, path)(f)
  def put(path:String)(f: Request => Response):Router = handle(PUT, path)(f)
  def delete(path:String)(f: Request => Response):Router = handle(DELETE, path)(f)
  def dispatch(): Unit
}

case class FastCGIRouter(handlers:Seq[Handler]) extends Router {
  def handle(method: Method, path:String)(f: Request => Response):Router = {
    return FastCGIRouter(Seq())
  }
  def dispatch(): Unit = {
    val header_buffer = stackalloc[Byte](8)
    val body_buffer = stackalloc[Byte](2048)
    System.err.println("reading from STDIN")
    while (true) {
      val header_read = stdio.fread(header_buffer,1,8,stdio.stdin)
      if (header_read == 0) {
        System.err.println("pipe closed, exiting")
        System.exit(0)
      }
      val header = readHeader(header_buffer,0)
      System.err.println(header)
      val content_read = stdio.fread(body_buffer,1,header.length + header.padding,stdio.stdin)

      System.err.println(s"read $header_read bytes header type ${header.rec_type} and $content_read body from stdin")
      // readAllHeaders(buffer,size_read)
      if ((header.rec_type == FCGI_STDIN) && (header.length == 0) ) {
        System.err.println("sending response")
        writeResponse(1, "Content-type: text/html\r\n\r\nhello")
        System.err.println("done")
      }
    }
  }
}

case class CGIRouter(handlers:Seq[Handler]) extends Router {
  def handle(method: Method, path:String)(f: Request => Response):Router = {
    val new_handler = Handler(method, CgiUtils.parsePathInfo(path), f)
    return CGIRouter(Seq(new_handler) ++ this.handlers)
  }

  def dispatch(): Unit = {
    val request = Router.parseRequest()
    val matches = for ( h @ Handler(method, pattern, handler) <- this.handlers
                        if request.method == method
                        if request.pathInfo.startsWith(pattern)) yield h
    val bestHandler = matches.maxBy( _.pattern.size )
    val response = bestHandler.handler(request)
    for ( (k,v) <- response.inferHeaders ) {
      System.out.println(k + ": " + v)
    }
    System.out.println()
    System.out.println(response.bodyToString)
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

  def init(mode:RouterMode = CGIMode):Router = {
    val errorResponse = Response(StringBody("No path matched the request"))
    val errorHandler = Handler(GET,List(), (_) => errorResponse)

    val debugHandler = Handler(GET,List("debug"), (request) => {
      Response(StringBody(request.toString))
    })

    val handlers:Seq[Handler] = List(debugHandler, errorHandler)
    mode match {
      case CGIMode => CGIRouter(handlers)
      case FCGIMode => FastCGIRouter(handlers)
    }
  }
}
