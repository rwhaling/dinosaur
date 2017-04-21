package io.dinosaur
import io.dinosaur._
object main {
  def main(args: Array[String]): Unit = {
    val router = Router.setup()
                       .handle("hello",200, _ => "Hello, world!")
    val response = router.dispatch()
  }
}
