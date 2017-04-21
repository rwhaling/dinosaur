# dinosaur
CGI-based web "framework" for Scala Native with the power of [Apache httpd](https://httpd.apache.org/).  

# what does it do?
Scala Native is the future, and a stunning achievement, but at the moment it only supports a small subset of the Java and C standard libraries, and it's especially light on the socket and networking front.  Dinosaur utilizes cutting-edge 1993 technology to allow it to serve up response to HTTP requests via the the Common Gateway Interface.

# simple example code
```scala
package io.dinosaur
import io.dinosaur._
object main { // No fancy DSL
  def main(args: Array[String]): Unit = {
    val request = Router.parseRequest()
    val router = Router.setup() // Reasonably fluent routing
                       .handle("/hello",200, _ => "Hello, world!")
    val response = router.dispatch(request)
  }
}
```

## simple access
```sh
$> http get 127.0.0.1:8080/cgi-bin/dinosaur/hello
HTTP/1.1 200 OK
Connection: Keep-Alive
Content-Length: 15
Content-Type: text/plain;charset=utf-8
Date: Fri, 21 Apr 2017 18:36:36 GMT
Keep-Alive: timeout=5, max=100
Server: Apache/2.4.18 (Ubuntu)

Hello, world!
```
Seriously.  That's it.

## no seriously what's going on
In a prehistoric time, before Rails and Java made enormous application server processes omnipresent , web programming used to be much simpler, and often more efficient.

How simple?  A CGI application receives all system and query parameters via the OS environment, plus an optional request body on STDIN -- it's then responsible for producing a legible HTTP response, including headers, on it's STDOUT stream. Processes are short-lived, share no state, and serve exactly one response before terminating.  This worked because Apache could load the interpreter for a language like Perl, (typically under 100kb), the actual handler script, and any dependencies in less than 20 ms -- 10 ms was more common.  

But as our languages and libraries improved, startup times and per-process memory usage increased multiplicatively. Forking a fresh process to serve each HTTP request was no longer practical.  Modern systems languages like Rust, Go, D, and Scala Native change these assumptions, however.  We can make self-contained packages, under 5 MB, that load and execute in 30ms with minimal overhead.  For other examples of this approach, see: golang's [net/http/cgi](https://golang.org/pkg/net/http/cgi/) and rust's [rust-scgi](https://github.com/ArtemGr/rust-scgi).

Best of all, the CGI API can be entirely and fluently expressed within the constraints of Scala Native's library support.  It reads files, it writes files, it does some light string manipulation, it looks up environment variables.  That's it.

# building and running

I develop on OS X Yosemite, which can only link Scala Native intermittently due to clang issues.  Fortunately, it compiles just fine, which greatly speeds up development cycles.  When I want to run it, I first do this:
```sh
$> docker build -f Dockerfile -t dinosaur .
```
which compiles and links the Scala Native binary, and deposits it in the `cgi-bin` dir of the apache server.  Then, I just:
```sh
$> docker run -d -p 8080:80 dinosaur
```
Which starts serving the app from the root URL: `http://DOCKER_HOST:8080/cgi-bin/dinosaur`, and then we can access via browser or CLI like so:

```sh
$> http get 127.0.0.1:8080/cgi-bin/dinosaur/hello
HTTP/1.1 200 OK
Connection: Keep-Alive
Content-Length: 15
Content-Type: text/plain;charset=utf-8
Date: Fri, 21 Apr 2017 18:36:36 GMT
Keep-Alive: timeout=5, max=100
Server: Apache/2.4.18 (Ubuntu)

Hello, world!
```

And that's all there is to it.

# TODO
 * Streamline these docker images.  I'd like to get it all ported to NixOS, and have separate images for build and runtime with a shared base.
 * JSON Parsing
 * HTTP Templating
 * Refined API, study existing Go and Rust models
 * Better model for distribution and inclusion as a library
 * Integrate with other web servers, esp. Nginx

# project status
No, seriously, this isn't an elaborate troll or rickroll. I love and miss old-school UNIX systems coding, and I did this because I love Scala and have been thinking a lot about ergonomics for an approachable web micro-framework, almost like Flask for Scala.  

That said, Scala Native is a very young project, and this is really purely speculative, research-quality, pre-release code for now. That said, I'd welcome outside contributions, issues, questions or comments.
