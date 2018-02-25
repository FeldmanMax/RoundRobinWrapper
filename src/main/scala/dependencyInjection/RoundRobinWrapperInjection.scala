package dependencyInjection

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import http.{HttpCall, HttpCallApi}
import net.codingwell.scalaguice.ScalaModule
import roundrobin.models.api.WeightRate
import services.HttpService
import translators.HttpErrorTranslator
import utils.{Configuration, RetryMechanism}
import wrappers.{ConnectionWrapper, HttpWrapper, LocalConnection}

class RoundRobinWrapperInjection extends AbstractModule with ScalaModule {
  def configure(): Unit = {
    general()
    weightRate()
    localConnection()
    http()
    services()
  }

  private def general() = {
    bind(classOf[RetryMechanism]).annotatedWith(Names.named("retry_mechanism")).to(classOf[RetryMechanism])
  }

  private def weightRate() = {
    bind(classOf[WeightRate]).annotatedWith(Names.named("increase_weight_rate")).toInstance(Configuration.increaseWeightRate())
      bind(classOf[WeightRate]).annotatedWith(Names.named("decrease_weight_rate")).toInstance(Configuration.decreaseWeightRate())
  }

  private def localConnection() = {
    bind(classOf[ConnectionWrapper]).annotatedWith(Names.named("local_connection")).to(classOf[LocalConnection])
  }

  private def http() = {
    bind(classOf[HttpCallApi]).annotatedWith(Names.named("http_call_api")).to(classOf[HttpCall])
    bind(classOf[HttpErrorTranslator]).annotatedWith(Names.named("http_error_translator")).to(classOf[HttpErrorTranslator])
  }

  private def services() = {
    bind(classOf[HttpService]).annotatedWith(Names.named("http_service")).to(classOf[HttpService])
    bind(classOf[HttpWrapper]).annotatedWith(Names.named("http_wrapper")).to(classOf[HttpWrapper])
  }
}
