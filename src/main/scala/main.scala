package io.dinosaur
import io.dinosaur._
object main {
  def main(args: Array[String]): Unit = {
    val request = Router.parseRequest()
    val router = Router.setup()
                       .handle("hello",200, _ => "Hello, world!")
    val response = router.dispatch(request)
    println("Content-Type: text/plain;charset=utf-8\n\n")
    println(response.body)
    // println(request)
  }
}
