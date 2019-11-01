package pme123.zio.examples.timpigden

import org.http4s.AuthedRoutes
import org.http4s.dsl.Http4sDsl
import pme123.zio.examples.timpigden.Authenticator.AuthToken
import zio.RIO
import zio.interop.catz._

class Hello2Service[R <: Authenticator] {

  type AuthenticatorTask[T] = RIO[R, T]
  private val dsl = Http4sDsl[AuthenticatorTask]
  import dsl._

  val service: AuthedRoutes[AuthToken, AuthenticatorTask] = AuthedRoutes.of[AuthToken, AuthenticatorTask] {
    case GET -> Root as authToken =>
      Ok(s"hello! ${authToken.tok}")
  }

}
