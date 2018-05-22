package Scala
import java.util
import java.util.LinkedHashMap
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

class SpringKdbLoadTester extends Simulation {
  //Need to change basicAuthHeader to throw non authenticated 401 error
  val basicAuthHeader = "Basic dXNlcjpwYXNz"
  val authUser= "connor"
  val authPass = "guessme"
  val baseUrl = "http://homer:8128"
  val contentType = "application/json"
  val endpoint = "/get"
  val requestCount = 10

  val httpProtocol = http
    .baseURL(baseUrl)
    .inferHtmlResources()
    .acceptHeader("application/xml, text/html, text/plain, application/json, */*")
    .acceptCharsetHeader("UTF-8")
    .acceptEncodingHeader("chunked")
    .authorizationHeader(basicAuthHeader)
    .contentTypeHeader(contentType)
    .userAgentHeader("curl/7.54.0")


  val headers_0 = Map(
    "Accept" -> "application/xml, text/html, text/plain, application/json, */*",
    "Accept-Encoding" -> "chunked")

  val scn: ScenarioBuilder = scenario("RecordedSimulation")
    .exec(http("request_0")
      .post(endpoint)
      .headers(headers_0)
      .basicAuth(authUser, authPass)
      .body(StringBody("""{"query":"`.gw.syncexec[\"select count i by date from trade where date=2018.04.10\";`hdb]"}""")).asJSON
      .check(status.is(200)))

  setUp(scn.inject(atOnceUsers(requestCount))).protocols(httpProtocol)

}