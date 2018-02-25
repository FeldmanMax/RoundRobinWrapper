package roundrobinwrapper.responses

import models.EndpointResponse
import utils.RetryMechanismResult

abstract class WrapperResponse[T]
case class HttpWrapperResponse[T](endpoint_response: EndpointResponse[T],
                                  retry_mechanism_response: RetryMechanismResult[EndpointResponse[T]]) extends WrapperResponse[T]