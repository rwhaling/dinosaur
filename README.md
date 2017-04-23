# dinosaur
Web "framework" for Scala Native with the power of [RFC 3875: The Common Gateway Interface](https://tools.ietf.org/html/rfc3875).  

## what does it do?
The CGI protocol is awesomely trivial: no threads, no network, no sockets, just STDIN, STDOUT, and environment variables. Dinosaur provides basic utilities for working with these primitives, but it also provides a straightforward Router API that should be familiar to anyone who's worked with Node, Flask, Sinatra, or the like.

But that's not all -- Dinosaur provides a Dockerfile for reproducible, containerized builds of your Scala Native app, as well as a built-in uWSGI web server.

## example code
```scala
package io.dinosaur.main
import io.dinosaur._

object main {
  def main(args: Array[String]): Unit = {
    Router.init()
          .get("/") { "<h1>Hello World!</h1>" }
          .dispatch()
  }
}
```

## how do i get it?
There is a giter8 template set up for dinosaur at [rwhaling/dinosaur.g8](https://github.com/rwhaling/dinosaur.g8).  You will need: *recent* SBT (0.13.13 or greater) and Docker.  Once you have that:
```sh
mkdir dinosaur-project
cd dinosaur-project
sbt new rwhaling/dinosaur.g8
docker build -t dinosaur-project .
docker run -d -p 8080:8080 dinosaur-project
```

If your SBT is older, it will tell you that it can't find the SBT new command.  I had to `brew update sbt` on my Mac to get a modern one, even though I've been building new-ish projects.

Once you have the container running, browse to `http://<YOUR_DOCKER_HOST>:8080/` to verify that it works.  From there, you can edit [src/main/scala/main.scala](src/main/scala/main.scala), and repeat the docker build / docker run procedure to test your work.  You can also run SBT compile outside of Docker to type-check your code.  

It's absolutely possible to build and test directly on a recent Mac as well, which I will document as soon as I upgrade.

## lean containers
Since the general Dockerfile is all-inclusive, it produces large-ish containers -- generally around 600 MB -- even though our executable is around 3-4 MB.  We can trim the fat by using one container for the build, with a volume mount for the output binary, and then use that to build a lean < 20 MB container.  It's not the default, because it's an extra step, but I'm kind of obsessive about this stuff.  Anyhow, if you want to do it:

```sh
docker build -f Dockerfile.build -t dinosaur-build .
docker -v $(pwd)/output:/output dinosaur-build

docker build -f Dockerfile.runtime -t tiny-dinosaur .
docker run -d -p 8080:8080 tiny-dinosaur
```

## TODO
 * Publish libraries to Bintray
 * Use Bintray library from G8 template
 * More examples
 * Static library linking would streamline and simplify the build process
 * JSON Parsing
 * HTTP Templating
 * Refined API, study existing Go and Rust models
 * Integrate with other web servers
 * Stress-testing and tuning uWSGI
 * More tests

## project status
No, seriously, this isn't an elaborate joke. I did this because I love old-school UNIX systems coding, and I did this because I love Scala and am super-stoked about Scala Native.  I've also been thinking a lot about what constitutes "vanilla" Scala style, and about ergonomics for an approachable web micro-framework, all of which inform the design of this project.

That said, Scala Native is a *very* young project, and this is really purely speculative, research-quality, pre-release code for now. That said, I'd welcome outside contributions, issues, questions or comments.
