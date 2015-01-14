package com.knoldus.spray.authentication.authenticators
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.knoldus.spray.authentication._
import spray.routing.HttpService._
import spray.routing._

object AppCredentialHandler {

  case class AppCredentialAuthenticator(val keys: List[String] = defaultKeys,
                                        val authenticator: Map[String, String] => Future[Option[Consumer]] = defaultAuthenticator)
                                        extends RestAuthenticator[Consumer] {
   
    def apply(): Directive1[Consumer] = authenticate(this)
  }

  val defaultKeys = List("app_key", "app_secret")
  val defaultAuthenticator = authFunction _

  def authFunction(params: Map[String, String]): Future[Option[Consumer]] = Future {
    val keyOpt = params.get(defaultKeys(0))
    val secretOpt = params.get(defaultKeys(1))

    val mayBeConsumer = for {
      key <- keyOpt
      secret <- secretOpt
      consumer <- {
        /*
          get user form database , replace None with proper method once database service is ready.
          getConsumer(key, secret)
         */
        None
      }
    } yield consumer
    mayBeConsumer
  }

}
