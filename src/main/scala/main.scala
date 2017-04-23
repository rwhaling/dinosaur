package io.dinosaur.main
import io.dinosaur._

object main {
  def main(args: Array[String]): Unit = {
    Router.init()
          .get("/")("<H1>Welcome to Dinosaur!</H1>")
          .get("/hello") { request =>
            "Hello World!"
          }
          .get("/who")( request =>
            request.pathInfo match {
              case Seq("who") => "Who's there?"
              case Seq("who",x) => "Hello, " + x
              case Seq("who",x,y) => "Hello, %s and %s".format(x,y)
              case _ => "Hello y'all!"
            }
          )
          .get("/bye")( request =>
            request.params.getOrElse("who",Seq.empty)
                   .map { x => "Bye, %s".format(x)}
                   .mkString(" ")
          )
          .dispatch()
  }
}
