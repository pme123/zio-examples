package pme123.zio.examples.timpigden.http

import org.http4s.dsl.Http4sDsl
import org.http4s.{AuthedRequest, AuthedRoutes}
import pme123.zio.examples.timpigden.http.Authenticator.AuthToken
import pme123.zio.examples.timpigden.http.XmlEncoders._
import zio.RIO
import zio.interop.catz._

class Hello4Service[R <: Authenticator] {

  type AuthTask[T] = RIO[R, T]
  private val dsl = Http4sDsl[AuthTask]
  import dsl._

  val service: AuthedRoutes[AuthToken, AuthTask] =
    AuthedRoutes.of[AuthToken, AuthTask] {
      case GET -> Root as authToken => Ok("hello4!")
      case GET -> Root / "president" as authToken =>
        Ok(Person.donald) // uses implicit encoder
      case AuthedRequest(authToken, req @ POST -> Root / "ageOf") =>
        req.decode[Person] { m =>
          Ok(m.age.toString)
        }
    }
}
