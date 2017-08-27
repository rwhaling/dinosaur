import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class ParameterizedSimulation extends Simulation {
  val url = System.getenv("GATLING_URL")
  val requests = Integer.parseInt(System.getenv("GATLING_REQUESTS"))
  val users = Integer.parseInt(System.getenv("GATLING_USERS"))
  val reqs_per_user = requests / users
  val rampTime = Integer.parseInt(System.getenv("GATLING_RAMP_TIME"))
  val scn = scenario("My scenario").repeat(reqs_per_user) {
    exec(
      http("Dinosaur")
        .get(url)
        .check(status.in(Seq(200,304)))
    )
  }

  setUp(scn.inject(rampUsers(users) over (rampTime seconds)))
}
