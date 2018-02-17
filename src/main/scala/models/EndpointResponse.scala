package models

import roundrobin.models.api.ConnectionResponse

case class EndpointResponse[T](connectionInfo: ConnectionResponse, response: T)
