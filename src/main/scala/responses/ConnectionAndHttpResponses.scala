package responses

import roundrobin.models.api.ConnectionResponse
import scalaj.http.HttpResponse

case class ConnectionAndHttpResponses(connection_response: ConnectionResponse, http_response: HttpResponse[String])
