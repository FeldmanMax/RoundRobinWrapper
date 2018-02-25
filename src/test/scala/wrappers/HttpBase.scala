package wrappers

import http.{HttpCall, HttpRequestMetadata}

trait HttpBase {
  def webServerWarmUp(port: Option[Int] = None): () => Option[String] = () => {
    def warmUp(current_retry: Int, max_retries: Int): Option[String] = {
      if(max_retries == 0)  Some(s"Reach $max_retries during warmUp")
      else  {
        val httpCall: HttpCall = new HttpCall
        val portToUse: String = port.getOrElse(80).toString
        httpCall.get(HttpRequestMetadata(s"http://localhost:$portToUse", 20000, 1000, Map.empty)) match {
          case Left(_) =>
            Thread.sleep(1000)
            warmUp(current_retry + 1, max_retries)
          case Right(_) => None
        }
      }
    }
    warmUp(1, 10)
  }
}
