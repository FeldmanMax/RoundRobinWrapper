package wrappers

import dwrapper.image.traits.DockerExecutor
import translators.HttpErrorTranslator
import org.scalatest.{BeforeAndAfter, FunSuite}
import roundrobin.api.ConnectionAPI
import roundrobin.models.api.WeightRate
import services.HttpService
import utils.{Configuration, HttpCall, RetryMechanism}

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
      val results: List[Either[String, Unit]] = List(test500(httpWrapper), test200(httpWrapper))
      if(results.exists(_.isLeft))  Option(results.filter(_.isLeft).map(_.left.get) mkString "\n")
      else                          None
    }, webServerWarmUp) match {
      case None => assert(1 == 1)
      case Some(error) => fail(error)
    }
  }

  private def test200(httpWrapper:HttpWrapper): Either[String, Unit] = {
    httpWrapper.get("http_response_200", Map.empty, (_: HttpResponse[String]) => {
      Right("all good")
    }) match {
      case Left(exception) => Left(exception)
      case Right(endpoint) => if(endpoint.response == "all good") Right()
                              else Left(endpoint.response)
    }
  }

  private def test500(httpWrapper:HttpWrapper): Either[String, Unit] = {
    httpWrapper.get("http_response_500", Map.empty, (_: HttpResponse[String]) => {
      fail("Got into the parser")
      Right("")
    }) match {
      case Left(exception) => if(exception != "Could not bring result for http_response_500 connection")  Left("test500 -> 1 ")
      else {
        ConnectionAPI.connectionWeight("http_response_500").right.flatMap { weight =>
          if(weight.totalWeight == 1) Right()
          else                        Left(s"test500 -> 2 with total weight ${weight.totalWeight} and not 1")
        }
      }
      case Right(endpoint) => fail(s"${endpoint.connectionInfo.endpointName} was returned ")
    }
  }

//  private def testDelay(httpWrapper:HttpWrapper): Either[String, Unit] = {
//
//  }

  private def getHttpResponseBody(response: HttpResponse[String]): Either[Exception, String] = {
    try {
      Right(response.body)
    }
    catch {
      case ex: Exception =>
        val retMessage: String = ex.getMessage
        Left(ex)
    }
  }
}
