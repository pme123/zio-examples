package pme123.zio.examples.timpigden

import cats.data.Kleisli
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import pme123.zio.examples.timpigden.Middlewares.withMiddleware
import zio.interop.catz._
import zio.test.Assertion._
import zio.test.{testM, _}

object Middlewares {
  val withMiddleware: AuthenticationMiddleware {
    type AppEnvironment = Authenticator
  } = new AuthenticationMiddleware {
    override type AppEnvironment = Authenticator
  }

  val hello2Service1: Hello2Service[Authenticator] = new Hello2Service[Authenticator]

  val hello2Service: Kleisli[withMiddleware.AppTask, Request[withMiddleware.AppTask], Response[withMiddleware.AppTask]] = Router[withMiddleware.AppTask](
    ("" -> withMiddleware.authenticationMiddleware(hello2Service1.service))
  ).orNotFound

  def authenticator: Authenticator = {
    new Authenticator {
      override val authenticatorService: Authenticator.Service =
        Authenticator.friendlyAuthenticator
    }
  }

  def requestWithAuth(
      uri: Uri = uri"/",
      pwd: String = "friend"
  ): Request[withMiddleware.AppTask] = {
    val req1 = Request[withMiddleware.AppTask](Method.GET, uri)
    AuthenticationHeaders.addAuthentication(req1, "tim", pwd)
  }
}
import pme123.zio.examples.timpigden.Middlewares._
object Hello2ServiceSpec
    extends DefaultRunnableSpec(
      suite("route s suites")(
        testM("root request returns forbidden") {
          val io = hello2Service
            .run(Request[withMiddleware.AppTask]())
            .provide(authenticator)
          assertM(io.map(_.status), equalTo(Status.Forbidden))
        },
        testM("root request returns Ok, when proper authenticated") {
          val io = hello2Service
            .run(requestWithAuth())
            .provide(authenticator)
          assertM(io.map(_.status), equalTo(Status.Ok))
        },
        testM("unmapped request returns not found") {
          val io = hello2Service
            .run(requestWithAuth(uri"/bad"))
            .provide(authenticator)
          assertM(io.map(_.status), equalTo(Status.NotFound))
        },
        testM("root request returns body 'hello! tim'") {
          val iop =
            (for {
              request <- hello2Service
                .run(requestWithAuth())
                .provide(authenticator)
              body <- request.bodyAsText.compile.toVector.map(_.mkString(""))
            } yield body).provide(authenticator)
          assertM(iop, equalTo("hello! tim"))
        },
        testM("bad password - request returns forbidden") {
          val io = hello2Service
            .run(requestWithAuth(uri"/", "badPassword"))
            .provide(authenticator)
          assertM(io.map(_.status), equalTo(Status.Forbidden))
        }
      )
    )
