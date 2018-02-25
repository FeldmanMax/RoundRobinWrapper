package wrappers

import com.google.inject.Inject
import com.google.inject.name.Named
import models.internal.ConnectionWeight
import roundrobin.api.ConnectionAPI
import roundrobin.models.api.{ConnectionResponse, EndpointWeight, WeightRate}

import scala.concurrent.Future

trait ConnectionWrapper {
  def next(connectionName : String) : Either[String, ConnectionResponse]
  def update(endpointName : String, isSuccess: Boolean) : Either[String, EndpointWeight]
  def updateToMin(endpointName: String): Either[String, EndpointWeight]
  def updateToMax(endpointName: String): Either[String, EndpointWeight]
  def nextAsync(connectionName : String) : concurrent.Future[Either[String, ConnectionResponse]]
  def updateAsync(endpointName : String, isSuccess: Boolean) : Future[Either[String, EndpointWeight]]
  def connectionWeight(connectionName : String) : Either[String, ConnectionWeight]
}

class LocalConnection @Inject() (@Named("increase_weight_rate") val increaseWeightRate: WeightRate,
                                 @Named("decrease_weight_rate") val decreaseWeightRate: WeightRate) extends ConnectionWrapper {
  def next(connectionName: String): Either[String, ConnectionResponse] = ConnectionAPI.next(connectionName)
  def update(endpointName: String, isSuccess: Boolean): Either[String, EndpointWeight] = ConnectionAPI.update(endpointName, getWeightRate(isSuccess))
  def updateToMin(endpointName: String): Either[String, EndpointWeight] = ConnectionAPI.update(endpointName, WeightRate(isSuccess = false, isPercent = false, 100))
  def updateToMax(endpointName: String): Either[String, EndpointWeight] = ConnectionAPI.update(endpointName, WeightRate(isSuccess = true, isPercent = false, 100))
  def nextAsync(connectionName: String): Future[Either[String, ConnectionResponse]] = ConnectionAPI.nextAsync(connectionName)
  def updateAsync(endpointName: String, isSuccess: Boolean): Future[Either[String, EndpointWeight]] = ConnectionAPI.updateAsync(endpointName, getWeightRate(isSuccess))
  def connectionWeight(connectionName: String): Either[String, ConnectionWeight] = ConnectionAPI.connectionWeight(connectionName)

  private def getWeightRate(isSuccess: Boolean): WeightRate = if(isSuccess) increaseWeightRate else decreaseWeightRate
}

class RemoteConnection @Inject()(@Named("uri")                  val uri: String,
                                 @Named("increase_weight_rate") val increaseWeightRate: WeightRate,
                                 @Named("decrease_weight_rate") val decreaseWeightRate: WeightRate) extends ConnectionWrapper {
  def next(connectionName: String): Either[String, ConnectionResponse] = ???
  def update(endpointName: String, isSuccess: Boolean): Either[String, EndpointWeight] = ???
  def updateToMin(endpointName: String): Either[String, EndpointWeight] = ???
  def updateToMax(endpointName: String): Either[String, EndpointWeight] = ???
  def nextAsync(connectionName: String): Future[Either[String, ConnectionResponse]] = ???
  def updateAsync(endpointName: String, isSuccess: Boolean): Future[Either[String, EndpointWeight]] = ???
  def connectionWeight(connectionName: String): Either[String, ConnectionWeight] = ???
}
