package io.dinosaur.main
import io.dinosaur._

object main {
  def main(args: Array[String]): Unit = {
    val mode = CgiUtils.env("ROUTER_MODE") match {
      case "FCGI" => FCGIMode
      case _      => CGIMode
    }
    Router.init(mode)
          .get("/")("<H1>Welcome to Dinosaur!</H1>")
          .get("/hello") { request =>
            "Hello World!"
          }
          .get("/who")( request =>
            request.pathInfo match {
              case Seq("who") => "Who's there?"
              case Seq("who",x) => "Hello, " + x
              case Seq("who",x,y) => "Hello both of you"
              case _ => "Hello y'all!"
            }
          )
          .get("/bye")( request =>
            request.params.getOrElse("who",Seq.empty)
                   .map { x => "Bye, " + x }
                   .mkString(". ")
          )
          .dispatch()
  }
}
