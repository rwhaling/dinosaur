package io.dinosaur.main
import io.dinosaur._
import scalanative.native._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object main {
  def main(args: Array[String]): Unit = {
    Router.init()
          .get("/")("<H1>Welcome to Dinosaur!</H1>")
          .get("/hello") { request =>
            "Hello World!"
          }
          .get("/who")( request =>
            request.pathInfo() match {
              case Seq("who") => "Who's there?"
              case Seq("who",x) => "Hello, " + x
              case Seq("who",x,y) => "Hello both of you"
              case _ => "Hello y'all!"
            }
          )
          .get("/bye")( request =>
            request.params("who")
                   .map { x => "Bye, " + x }
                   .mkString(". ")
          )
          .dispatch()
  }
}
