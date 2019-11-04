package pme123.zio.examples.timpigden.http

import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import zio._
import zio.clock.Clock
import zio.console._
import zio.interop.catz._

object Hello4App extends App with AuthenticationMiddleware {

  type AppEnvironment = Authenticator with Clock

  val hello4Service = new Hello4Service[AppEnvironment] {}

  val authenticatedService = authenticationMiddleware(hello4Service.service)

  val secApp = Router[AppTask](
    "" -> authenticatedService
  ).orNotFound

  val server1 = ZIO.runtime[AppEnvironment]
    .flatMap {
      implicit rts =>
        BlazeServerBuilder[AppTask]
          .bindHttp(9009, "localhost")
          .withHttpApp(secApp)
          .serve
          .compile
          .drain
    }

  val server = server1
    .provideSome[ZEnv] { base =>
      new  Authenticator with Clock {
        override val clock: Clock.Service[Any] = base.clock

        override def authenticatorService: Authenticator.Service = Authenticator.friendlyAuthenticator
      }
    }

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    server.foldM(err => putStrLn(s"execution failed with $err") *> ZIO.succeed(1), _ => ZIO.succeed(0))
}