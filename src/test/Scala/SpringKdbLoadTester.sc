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
  val authUser= "user"
  val authPass = "pass"
  val baseUrl = "http://localhost:8080"
  val contentType = "application/json"
  val endpoint = "/executeFunction"
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
      .body(StringBody("{{\"function_name\":\"plus\",\"arguments\":{\"xarg\" : \"90.3\",\"yarg\": \"9.7\"}}")).asJSON
      .check(status.is(200)))

  setUp(scn.inject(atOnceUsers(requestCount))).protocols(httpProtocol)

}