package com.knoldus.spray.authentication

import org.scalatest.FlatSpec
import spray.testkit.ScalatestRouteTest
import org.scalatest.Matchers
import spray.routing.HttpService
import spray.http.HttpResponse
import spray.routing._
import spray.http.StatusCodes._
import scala.concurrent.Future
import TestAuthHandler._
import spray.routing.Directive
import scala.concurrent.{ ExecutionContext, Future }
import ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.{ ExecutionContext, Future }
import ExecutionContext.Implicits.global
import spray.routing._
import spray.routing.HttpService._
import spray.routing.authentication._
import spray.routing.AuthenticationFailedRejection.CredentialsMissing
import spray.routing.AuthenticationFailedRejection.CredentialsRejected
import scala.concurrent.duration.FiniteDuration
import akka.util.Timeout
import java.util.concurrent.TimeUnit.SECONDS
import com.knoldus.spray.authentication.authenticators.AccessTokenHandler
import com.knoldus.spray.authentication.authenticators.UserPassHandler
import com.knoldus.spray.authentication.authenticators.AppCredentialHandler

class RestAuthenticatorTest extends FlatSpec with ScalatestRouteTest with Matchers with HttpService {

  val actorRefFactory = system
  implicit val timeout = Timeout(new FiniteDuration(15, SECONDS))
  implicit val routeTestTimeout = RouteTestTimeout(timeout.duration)

  val accessTokenHandlerForTest = AccessTokenHandler.AccessTokenAuthenticator(authenticator = TestAccessTokenHandler.authenticator).apply()
  val userPassHandlerForTest = UserPassHandler.UserPassAuthenticator(authenticator = TestUserPassHandler.authenticator).apply()
  val appCredentialHandlerForTest = AppCredentialHandler.AppCredentialAuthenticator(authenticator = TestAppCredentialHandler.authenticator).apply()

  /**
   *    Following resources are not protected.
   */

  val openRoutes = {
    get {
      pathSingleSlash
      path("ping") {
        complete(HttpResponse(OK, "WELCOME"))
      }
    }
  }

  val tokenProtectedRoutes: Route = {
    pathPrefix("user") {
      accessTokenHandlerForTest { user =>

        /*
         *    Following are protected resources.
         *   Requires valid access_token of p3 user.
         */

        get {
          path("items") {
            complete(HttpResponse(OK, "Display user's item"))
          }
        }

      }
    }
  }

  /**
   * Following route shows how authenticators can be composed. Here userPassHandlerForTest &
   * appCredentialHandlerForTest has been composed to validate app and user's credentials
   */

  val userPassProtectedRoutes = {
    pathPrefix("authenticate") {
      userPassHandlerForTest { user =>
        appCredentialHandlerForTest { consumer =>
          /*
            *   Following are protected resources.
            *   Request would gone through userPass and appCredential authenticator to unlock the resources.
            */

          get {
            pathSingleSlash {
              complete(HttpResponse(OK, "Welcome User !!!"))
            }
          }
        }
      }
    }
  }

  val consumerKeyProtectedRoutes = {
    pathPrefix("app") {
      appCredentialHandlerForTest { consumer =>
        userPassHandlerForTest { user =>
          /*
         *   Following are protected resources.
         *   Requires valid consumer_key & consumer_secret + User's credentials to unlock the resources.
         */

          get {
            path("balance") {
              complete(HttpResponse(OK, "Need to recharge :)"))
            }
          }
        }
      }
    }
  }

  val testRoutes = openRoutes ~ pathPrefix("secure")(tokenProtectedRoutes ~ userPassProtectedRoutes ~ consumerKeyProtectedRoutes)

  //####################################################################
  //TEST CASE FOR UNPROTECTED RESOURCES 
  //####################################################################

  "The service" should
    "respond to unprotected resource" in {
      Get("/ping") ~> testRoutes ~> check {
        responseAs[String] should include("WELCOME")
      }
    }

  //####################################################################
  //TEST CASES FOR ACCESS TOKEN PROTECTED RESOURCES 
  //####################################################################

  "The service" should
    "respond to accessToken protected resources if valid access token is provided" in {
      Get(s"/secure/user/items?access_token=$validAccessTokenString") ~> testRoutes ~> check {
        responseAs[String] should include("Display user's item")
      }
    }

  "The service" should
    "not respond to accessToken protected resource if access token is not valid" in {
      val invalidAccessToken = "sfd3454543ergr"
      Get(s"/secure/user/items?access_token=$invalidAccessToken") ~> testRoutes ~> check {
        rejection === AuthenticationFailedRejection(CredentialsRejected, List())
      }
    }

  "The service" should
    "not respond to accessToken protected resource if access token is missing" in {
      Get(s"/secure/user/items") ~> testRoutes ~> check {
        rejection === AuthenticationFailedRejection(CredentialsMissing, List())
      }
    }

  //####################################################################
  //TEST CASES FOR APP KEY/SECRET PROTECTED RESOURCES 
  //####################################################################

  "The service" should
    "respond to App-KeySecret protected resources if valid key & secret is provided" in {
      Get(s"/secure/app/balance?app_key=$validAppKey&app_secret=$validAppSecret") ~> testRoutes ~> check {
        responseAs[String] should include("Need to recharge")
      }
    }

  "The service" should
    "not respond to App-KeySecret protected resource if key or secret is not valid" in {
      val invalidAppKey = "sfd3454543ergr"
      Get(s"/secure/app/balance?app_key=$invalidAppKey&app_secret=$validAppSecret") ~> testRoutes ~> check {
        rejection === AuthenticationFailedRejection(CredentialsRejected, List())
      }
    }

  "The service" should
    "not respond to App-KeySecre protected resource if key or secret is missing" in {
      Get(s"/secure/app/balance") ~> testRoutes ~> check {
        rejection === AuthenticationFailedRejection(CredentialsMissing, List())
      }
    }

}