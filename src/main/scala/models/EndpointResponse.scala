package models

case class EndpointResponse[T](connectionInfo: ConnectionResponse, response: T)
