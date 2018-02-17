package utils

import com.typesafe.config.{Config, ConfigFactory}
import roundrobin.models.api.WeightRate

object Configuration {
  private val config: Config = ConfigFactory.load()

  def isProduction: Boolean = AppConfiguration.isProduction
  def isTest: Boolean = AppConfiguration.isTest
  def maxRetries: Int = config.getInt("maxRetries")

  def httpConnectionTimeoutInMillis: Int = config.getInt("httpConnectionTimeoutInMillis")
  def httpReadTimeoutInMillis: Int = config.getInt("httpReadTimeoutInMillis")

  def increaseWeightRate(): WeightRate = WeightRate(isSuccess = true, isPercent = config.getBoolean("weightRate.increase.isPercent"), quantity = config.getInt("weightRate.increase.quantity"))
  def decreaseWeightRate(): WeightRate = WeightRate(isSuccess = false, isPercent = config.getBoolean("weightRate.decrease.isPercent"), quantity = config.getInt("weightRate.decrease.quantity"))
}
