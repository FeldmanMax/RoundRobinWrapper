package wrappers

import roundrobinwrapper.responses.WrapperResponse

trait BaseWrapper[Source] {
  /**
    * Returns the result of the request
    *
    * @param connectionName - connection which will round robin will work on
    * @param params         - parameters which should be passed for the request
    * @param translate      - method which translates the result to a internal module
    * @return
    */
  def get[T](connectionName: String,
             params: Map[String, String],
             translate: (Source) => Either[Exception, T]): Either[String, WrapperResponse[T]]
}
