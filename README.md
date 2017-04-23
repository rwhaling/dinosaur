# dinosaur
Web "framework" for Scala Native with the power of [RFC 3875: The Common Gateway Interface](https://tools.ietf.org/html/rfc3875).  

## what does it do?
The CGI protocol is awesomely trivial: no threads, no network, no sockets, just STDIN, STDOUT, and environment variables. Dinosaur provides basic utilities for working with these primitives, but it also provides a straightforward Router API that should be familiar to anyone who's worked with Node, Flask, Sinatra, or the like.

But that's not all -- Dinosaur provides a Dockerfile for reproducible, containerized builds of your Scala Native app, as well as a built-in uWSGI web server.

## example code
```scala
package io.dinosaur
import io.dinosaur._
object main {
  def main(args: Array[String]): Unit = {
    val request = Router.parseRequest()
    val router = Router.setup()
                       .handle("/hello",200, _ => "Hello, world!")
    val response = router.dispatch(request)
  }
}
```

## how do i get it?



## building and running

Scala Native produces tiny binaries, but the SBT build toolchain is unfortunately quite large.  To address this, Dinosaur includes 2 separate Dockerfiles for build and runtime, with helper shell scripts. So although the build image is around 400 MB, the runtime is typically well under 20 MB, including the uWSGI server.

{REVISE}

I develop on OS X Yosemite, which can only link Scala Native intermittently due to clang issues.  Fortunately, it compiles just fine, which greatly speeds up development cycles.  When I want to run
it, I first do this:
```sh
$> docker build -f Dockerfile -t dinosaur .
```
which compiles and links the Scala Native binary, and deposits it in the `cgi-bin` dir of the apache server.  Then, I just:
```sh
$> docker run -d -p 8080:80 dinosaur
```

Which starts serving the app from the root URL: `http://DOCKER_HOST:8080/cgi-bin/dinosaur`, and then we can access via browser or CLI like so:

```sh
$> http get localhost:8080/hello
HTTP/1.1 200 OK
Connection: Keep-Alive
Content-Length: 15
Content-Type: text/plain;charset=utf-8
Date: Fri, 21 Apr 2017 18:36:36 GMT
Keep-Alive: timeout=5, max=100
Server: Apache/2.4.18 (Ubuntu)

Hello, world!
```

## TODO
 * Publish libraries to Bintray
 * g8 template for `sbt new dinosaur`
 * Static library linking would streamline and simplify the build process
 * JSON Parsing
 * HTTP Templating
 * Refined API, study existing Go and Rust models
 * Integrate with other web servers
 * Stress-testing and tuning uWSGI

## project status
No, seriously, this isn't an elaborate troll. I did this because I love old-school UNIX systems coding, and I did this because I love Scala and am super-stoked about Scala Native.  I've also been thinking a lot about "vanilla" Scala style, and ergonomics for an approachable web micro-framework, all of which inform the design of this project.

That said, Scala Native is a *very* young project, and this is really purely speculative, research-quality, pre-release code for now. That said, I'd welcome outside contributions, issues, questions or comments.
