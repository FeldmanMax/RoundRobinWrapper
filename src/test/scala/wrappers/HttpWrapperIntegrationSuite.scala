package wrappers

import dwrapper.image.traits.DockerExecutor
import http.HttpCall
import translators.HttpErrorTranslator
import org.scalatest.{BeforeAndAfter, FunSuite}
import roundrobin.api.ConnectionAPI
import services.HttpService
import utils.{Configuration, RetryMechanism}

import scalaj.http.HttpResponse

class HttpWrapperIntegrationSuite extends FunSuite with DockerExecutor with HttpBase with BeforeAndAfter {

  private var httpWrapper:HttpWrapper = _
  private var localConnection = new LocalConnection(Configuration.increaseWeightRate(), Configuration.decreaseWeightRate())
  before {
    localConnection = new LocalConnection(Configuration.increaseWeightRate(), Configuration.decreaseWeightRate())
    httpWrapper = new HttpWrapper(new RetryMechanism, localConnection, new HttpService(new HttpCall, new HttpErrorTranslator))
  }

  test("HTTP calls") {
    val dockerFilePath: String = "/Users/maksik1/IdeaProjects/ApiResponse"
    dockerExecute("http-calls-2.0.0", "image/web-api", "2.0.0", dockerFilePath, Some(Map("80" -> "9000"))) ({ () =>
      val results: List[Either[String, Unit]] = runTestList()
      if(results.exists(_.isLeft))  Option(results.filter(_.isLeft).map(_.left.get) mkString "\n")
      else                          None
    }, webServerWarmUp()) match {
      case None => assert(1 == 1)
      case Some(error) => fail(error)
    }
  }

  private def runTestList(): List[Either[String, Unit]] = {
    List(
      test500(httpWrapper),
      test200(httpWrapper),
      testDelay(httpWrapper),
      testParserThrowsAnException(httpWrapper)
    )
  }

  private def test200(httpWrapper:HttpWrapper): Either[String, Unit] = {
    httpWrapper.get("http_response_200", Map.empty, (_: HttpResponse[String]) => {
      Right("all good")
    }) match {
      case Left(exception) => Left(exception)
      case Right(endpoint) => if(endpoint.endpoint_response.response == "all good") Right()
                              else Left(endpoint.endpoint_response.response)
    }
  }

  private def test500(httpWrapper:HttpWrapper): Either[String, Unit] = {
    httpWrapper.get("http_response_500", Map.empty, (_: HttpResponse[String]) => {
      fail("Got into the parser")
      Right("")
    }) match {
      case Left(exception) => if(exception.contains("Could not bring result for http_response_500 connection") &&
                                  // means that there were no retries because of the nature of the error
                                 exception.contains("Had an unrecoverable problem"))  Left("test500 -> 1 ")
      else {
        // No retry was supposed to be performed. This is a 500 and NOT a recoverable exception
        // It's possible that what make this endpoint to go down is the data which is passed
        ConnectionAPI.connectionWeight("http_response_500").right.flatMap { weight =>
          if(weight.totalWeight == 1)  Right()
          else                          Left(s"test500 -> 2 with total weight ${weight.totalWeight} and not 1")
        }
      }
      case Right(wrappedResponse) =>
        fail(s"${wrappedResponse.endpoint_response.connectionInfo.endpointName} was returned ")
    }
  }

  private def testDelay(httpWrapper:HttpWrapper): Either[String, Unit] = {
    var prev_delay_2_seconds = ConnectionAPI.connectionWeight("http_response_delay_2_seconds").right.flatMap { weight => Right(weight) }.right.get

    var result: Either[String, Unit] = Left("")
    (0 until 10).foreach { _ =>
      httpWrapper.get("http_response_delay_and_good_response", Map.empty, (_: HttpResponse[String]) => {
        Right("")
      }) match {
        case Left(exception) => Left(exception)
        case Right(endpoint) =>
          if(endpoint.endpoint_response.connectionInfo.endpointName != "no_delay")  result = Left(result.left.get + "\n NOT no_delay")
          else {
            val penalty: Int = endpoint.retry_mechanism_response.retries * localConnection.decreaseWeightRate.quantity
            ConnectionAPI.connectionWeight("http_response_delay_2_seconds").right.flatMap { weight =>
              if(prev_delay_2_seconds.totalWeight == 1 && weight.totalWeight == 1) {
                prev_delay_2_seconds = weight
                Right()
              }
              else {
                if(prev_delay_2_seconds.totalWeight - penalty != weight.totalWeight)
                  Left(result.left.get + s"\n ${prev_delay_2_seconds.totalWeight} - $penalty != ${weight.totalWeight}")
                else  Right()
              }
            }
          }
      }
    }
    if(result.left.get.isEmpty) Right()
    else                        result
  }

  private def testParserThrowsAnException(httpWrapper: HttpWrapper): Either[String, Unit] = {
    httpWrapper.get("http_response_no_delay", Map.empty, (_: HttpResponse[String]) => {
      val result: Int = 1/0
      Right("")
    }) match {
      case Left(exception) => if(!exception.contains("by zero")) Left(s"testParserThrowsAnException -> no by zero!! Exception: $exception")
      else if(ConnectionAPI.connectionWeight("http_response_no_delay").right.get.totalWeight != 100) Left("!= 100")
      else Right()
      case Right(_) => Left("testParserThrowsAnException -> Got response")
    }
  }
}