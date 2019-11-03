package pme123.zio.examples.timpigden

import cats.data.{Kleisli, OptionT}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedRoutes, Header, Request}
import pme123.zio.examples.timpigden.Authenticator.{AuthToken, AuthenticationError}
import zio.interop.catz._
import zio.{IO, RIO, Task, ZIO}

import scala.language.higherKinds

trait Authenticator { def authenticatorService: Authenticator.Service }

object Authenticator {

  case class AuthToken(tok: String)

  trait AuthenticationError extends Throwable

  val authenticationError: AuthenticationError = new AuthenticationError {
    override def getMessage: String = "Authentication Error"
  }

  trait Service {
    def authenticate(userName: String, password: String): Task[AuthToken]
  }

  val friendlyAuthenticator: Service = { (userName, password) =>
    password match {
      case "friend" =>
        IO.succeed(AuthToken(userName)) // rather trivial implementation but does allow us to inject variety
      case _ => IO.fail(authenticationError)
    }
  }

}
package object authenticator {
  def authenticatorService: ZIO[Authenticator, AuthenticationError, Authenticator.Service] = ZIO.accessM(x => ZIO.succeed(x.authenticatorService))
}
trait AuthenticationHeaders[R <: Authenticator] {
  type AuthHTask[T] = RIO[R, T]

  private def unauthenticated = IO.succeed(Left(new Exception("bad format authentication")))

  def getToken(req: Request[AuthHTask]) : AuthHTask[Either[Throwable, AuthToken]] = {
    val userNamePasswordOpt: Option[Array[String]] =
      for {
        auth <- req.headers.get(Authorization).map(_.value)
        asSplit = auth.split(" ")
        if asSplit.size == 2
      } yield asSplit
    val tok = userNamePasswordOpt.map { asSplit =>
      val res1 = for {
        authentic <- authenticator.authenticatorService
        tok  <- authentic.authenticate(asSplit(0), asSplit(1))
      } yield tok
      res1.either
    }
    tok.getOrElse(unauthenticated)
  }
}

trait AuthenticationMiddleware {

  type AppEnvironment <: Authenticator
  type AppTask[A] = RIO[AppEnvironment, A]

  val dsl: Http4sDsl[AppTask] = Http4sDsl[AppTask]
  import dsl._

  val authenticationHeaders = new AuthenticationHeaders[AppEnvironment] {}

  def authUser: Kleisli[AppTask, Request[AppTask], Either[String, AuthToken]] = {
    Kleisli({ request =>
      authenticationHeaders.getToken(request).map { e => {
        e.left.map (_.toString)
      }}
    }
    )
  }

  val onFailure: AuthedRoutes[String, AppTask] = Kleisli(req => OptionT.liftF {
    Forbidden(req.authInfo)
  })

  val authenticationMiddleware: AuthMiddleware[AppTask, AuthToken] = AuthMiddleware(authUser, onFailure)
}

object AuthenticationHeaders {
  def addAuthentication[Tsk[_]](request: Request[Tsk], username: String, password: String): Request[Tsk] =
    request.withHeaders(request.headers.put(Header("Authorization", s"$username $password")))
}