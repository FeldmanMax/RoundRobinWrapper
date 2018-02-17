import org.scalatest.FunSuite
import roundrobin.api.ConnectionAPI

class ConfigurationSuite extends FunSuite {
  test("http_response_200 load") {
    ConnectionAPI.next("http_response_200") match {
      case Left(left) => fail(left)
      case Right(response) => assert(response.connectionName == "http_response_200")
    }
  }
}
