# dinosaur
Web "framework" for Scala Native with the power of [RFC 3875: The Common Gateway Interface](https://tools.ietf.org/html/rfc3875).  

## what does it do?
The CGI protocol is awesomely trivial: no threads, no network, no sockets, just STDIN, STDOUT, and environment variables--which happens to align with the bare-metal power of Scala Native. Dinosaur provides basic utilities for working with these primitives, but it also provides a straightforward Router API that should be familiar to anyone who's worked with Node, Flask, Sinatra, or the like.

But that's not all -- Dinosaur provides a Dockerfile for reproducible, containerized builds of your Scala Native app, as well as a built-in uWSGI web server.

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
Clone the example project at  [rwhaling/dinosaur-example-project](https://github.com/rwhaling/dinosaur-example-project).  You will need Git and Docker.  Once you have that:
```sh
git clone https://github.com/rwhaling/dinosaur-example-project
cd dinosaur-example-project
docker build -t dinosaur-project .
docker run -d -p 8080:8080 dinosaur-project
<navigate to port 8080 on your docker host>
```

Setting up Scala Native for local builds is outside the scope of this documentation, but well documented [on the main Scala Native site](http://www.scala-native.org/en/latest/user/setup.html).

## lean containers
Since the general Dockerfile is all-inclusive, it produces large-ish containers -- generally around 600 MB -- even though our executable is around 3-4 MB.  We can trim the fat by using one container for the build, with a volume mount to catch the output binary, and then use that to build a lean < 20 MB container:

```sh
docker build -f Dockerfile.build -t dinosaur-build .
docker run -v $(pwd)/output:/output dinosaur-build

docker build -f Dockerfile.runtime -t tiny-dinosaur .
docker run -d -p 8080:8080 tiny-dinosaur
```

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
 * Stress-testing and tuning uWSGI

## project status
No, seriously, this isn't an elaborate joke. I did this because I love old-school UNIX systems coding, and I did this because I love Scala and am super-stoked about Scala Native.  I've also been thinking a lot about what constitutes "vanilla" Scala style, and about ergonomics for an approachable web micro-framework, all of which inform the design of this project.

That said, Scala Native is a *very* young project, and this is really purely speculative, research-quality, pre-release code for now. That said, I'd welcome outside contributions, issues, questions or comments.
