package api

import dwrapper.image.traits.DockerExecutor
import org.scalatest.FunSuite
import roundrobinwrapper.api.RoundRobinApi
import wrappers.HttpBase

import scalaj.http.HttpResponse

class RoundRobinApiTest extends FunSuite with DockerExecutor with HttpBase {
  test("API - get") {
    val dockerFilePath: String = "/Users/maksik1/IdeaProjects/ApiResponse"
    dockerExecute("http-calls-3.0.0", "image/web-api", "2.0.0", dockerFilePath, Some(Map("82" -> "9000"))) ({ () =>
      val results: List[Either[String, Unit]] = runTestList()
      if(results.exists(_.isLeft))  Option(results.filter(_.isLeft).map(_.left.get) mkString "\n")
      else                          None
    }, webServerWarmUp(Option(82))) match {
      case None => assert(1 == 1)
      case Some(error) => fail(error)
    }
  }

  private def runTestList(): List[Either[String, Unit]] = {
    List(testApiGet())
  }

  private def testApiGet(): Either[String, Unit] = {
    RoundRobinApi.get("http_response_no_delay_82", Map.empty, (_: HttpResponse[String]) => {
      Right("all good")
    }) match {
      case Left(exception) => Left(exception)
      case Right(endpoint) => if(endpoint.endpoint_response.response == "all good") Right()
      else Left(endpoint.endpoint_response.response)
    }
  }
}
