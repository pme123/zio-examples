package pme123.zio.examples.timpigden.http

import cats.data.Kleisli
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import zio.interop.catz._

object Middlewares {
  val withMiddleware: AuthenticationMiddleware {
    type AppEnvironment = Authenticator
  } = new AuthenticationMiddleware {
    override type AppEnvironment = Authenticator
  }

  private val hello2Service1: Hello2Service[Authenticator] =
    new Hello2Service[Authenticator]

  val hello2Service: Kleisli[withMiddleware.AppTask, Request[
    withMiddleware.AppTask
  ], Response[withMiddleware.AppTask]] =
    Router[withMiddleware.AppTask](
      "" -> withMiddleware.authenticationMiddleware(hello2Service1.service)
    ).orNotFound

  private val hello4Service1: Hello4Service[Authenticator] =
    new Hello4Service[Authenticator]

  val hello4Service: Kleisli[withMiddleware.AppTask, Request[
    withMiddleware.AppTask
  ], Response[withMiddleware.AppTask]] =
    Router[withMiddleware.AppTask](
      "" -> withMiddleware.authenticationMiddleware(hello4Service1.service)
    ).orNotFound

  def authenticator: Authenticator = {
    new Authenticator {
      override val authenticatorService: Authenticator.Service =
        Authenticator.friendlyAuthenticator
    }
  }

  def requestWithAuth(
      uri: Uri = uri"/",
      pwd: String = "friend",
      method: Method = Method.GET
  ): Request[withMiddleware.AppTask] = {
    val req1 = Request[withMiddleware.AppTask](method, uri)
    AuthenticationHeaders.addAuthentication(req1, "tim", pwd)
  }
}
