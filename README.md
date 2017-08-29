# dinosaur
Web "framework" for Scala Native with the power of [RFC 3875: The Common Gateway Interface](https://tools.ietf.org/html/rfc3875).  

## what does it do?
The CGI protocol is awesomely trivial: no threads, no network, no sockets, just STDIN, STDOUT, and environment variables--which happens to align with the bare-metal power of Scala Native. Dinosaur provides basic utilities for working with these primitives, but it also provides a straightforward Router API that should be familiar to anyone who's worked with Node, Flask, Sinatra, or the like.

But that's not all -- Dinosaur provides a Dockerfile for reproducible, containerized builds of your Scala Native app, as well as a built-in Apache httpd web server.

## example code
```scala
package io.dinosaur.main
import io.dinosaur._

object main {
  def main(args: Array[String]): Unit = {
    Router.init()
          .get("/") { "<h1>Hello World!</h1>" }
          .get("/foo") { request => "bar" }
          .dispatch()
  }
}
```

## how do i get it?
I'm still working on distributing Dinosaur as a Bintray package. Since that's not stable yet, I would recommend cloning this project and editing main.scala for now.(
  
Setting up Scala Native for local builds is outside the scope of this documentation, but well documented [on the main Scala Native site](http://www.scala-native.org/en/latest/user/setup.html).

## lean containers
Although Scala Native produces tiny executables, the full SBT/JDK stack can push the size of an all-inclusive docker container up to about 600 MB.  Dinosaur's Dockerfile uses multi-stage builds to separate the process into phases, and only copies binary artifcats into the final container.  Note that this technique requires a recent version of Docker, 17.05 or newer.  

## TODO
 * Working g8/sbt new integration
 * More examples
 * More documentation
 * More tests
 * Exception-based error code handling
 * Chunked transport for streaming
 * Static linking
 * JSON Parsing
 * HTTP Templating
 * Refined API, study existing Go and Rust models
 * Integrate with other web servers
 * Stress-testing and tuning Apache

## project status
No, seriously, this isn't an elaborate joke. I did this because I love old-school UNIX systems coding, and I did this because I love Scala and am super-stoked about Scala Native.  I've also been thinking a lot about what constitutes "vanilla" Scala style, and about ergonomics for an approachable web micro-framework, all of which inform the design of this project.

That said, Scala Native is a *very* young project, and this is really purely speculative, research-quality, pre-release code for now. That said, I'd welcome outside contributions, issues, questions or comments.
