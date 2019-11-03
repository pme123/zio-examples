package pme123.zio.examples.timpigden

import org.http4s._
import org.http4s.implicits._
import pme123.zio.examples.timpigden.Middlewares.{withMiddleware, _}
import zio.interop.catz._
import zio.test.Assertion._
import zio.test.{testM, _}

object Hello2ServiceSpec
    extends DefaultRunnableSpec(
      suite("Hello2ServiceSpec routes suites")(
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
