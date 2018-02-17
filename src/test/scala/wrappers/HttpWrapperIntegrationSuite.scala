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

  test("google search - bitcoin info") {
    val searchKeyWord: String = "bitcoin"
    val connectionRetriever: ConnectionWrapper = new LocalConnection(WeightRate(isSuccess = true, isPercent = false, 10), WeightRate(isSuccess = false, isPercent = false, 20))
    val httpService: HttpService = new HttpService(new HttpCall, new HttpErrorTranslator)
    val httpWrapper: HttpWrapper = new HttpWrapper(new RetryMechanism(), connectionRetriever, httpService)
    val result = httpWrapper.get[String]("search_google", Map("q" -> searchKeyWord), getHttpResponseBody)
    result match {
      case Left(message) => fail(message)
      case Right(data) =>
        val pattern = searchKeyWord.r
        val amountOfOccurences = (pattern findAllIn data.response.toLowerCase).toList.size
        assert(amountOfOccurences > 1, data.response)
    }
  }

  test("HTTP calls") {
    val dockerFilePath: String = "/Users/maksik1/IdeaProjects/ApiResponse"
    dockerExecute("web-api-2.0.0", "image/web-api", "2.0.0", dockerFilePath, Some(Map("80" -> "9000"))) ({ () =>
//      test200(httpWrapper)
//      test300(httpWrapper)
//      test400(httpWrapper)
      val results: List[Either[String, Unit]] = List(test500(httpWrapper))
      if(results.exists(_.isLeft))  Option(results.filter(_.isLeft).map(_.left.get) mkString "\n")
      else                          None
    }, webServerWarmUp) match {
      case None => assert(1 == 1)
      case Some(error) => fail(error)
    }
  }

  private def test200(httpWrapper:HttpWrapper): Unit = {
    httpWrapper.get("http_response_200", Map.empty, (_: HttpResponse[String]) => {
      fail("Got into the parser")
      Right("")
    }) match {
      case Left(exception) => assert(exception == "Could not bring result for http_response_500 connection")
      case Right(endpoint) => fail(s"${endpoint.connectionInfo.endpointName} was returned ")
    }
  }

  private def test300(httpWrapper:HttpWrapper): Unit = {
    httpWrapper.get("http_response_300", Map.empty, (_: HttpResponse[String]) => {
      fail("Got into the parser")
      Right("")
    }) match {
      case Left(exception) => assert(exception == "Could not bring result for http_response_500 connection")
      case Right(endpoint) => fail(s"${endpoint.connectionInfo.endpointName} was returned ")
    }
  }

  private def test400(httpWrapper:HttpWrapper): Unit = {
    httpWrapper.get("http_response_400", Map.empty, (_: HttpResponse[String]) => {
      fail("Got into the parser")
      Right("")
    }) match {
      case Left(exception) => assert(exception == "Could not bring result for http_response_500 connection")
      case Right(endpoint) => fail(s"${endpoint.connectionInfo.endpointName} was returned ")
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
