package pme123.zio.examples.timpigden.http

import org.http4s._
import org.http4s.implicits._
import zio.interop.catz._
import zio.test.Assertion.equalTo
import zio.test.{DefaultRunnableSpec, assertM, suite, testM, _}

object Hello1ServiceSpec
    extends DefaultRunnableSpec(
      suite("Hello1ServiceSpec routes suites")(
        testM("root request returns ok") {
          val io = for {
            response <- Hello1Service.service.run(Request())
          } yield response.status
          assertM(io, equalTo(Status.Ok))
        },
        testM("root request returns Ok, using assertM insteat") {
          assertM(
            Hello1Service.service.run(Request()).map(_.status),
            equalTo(Status.Ok)
          )
        },
        testM("wrong url request returns NotFound, using assertM insteat") {
          assertM(
            Hello1Service.service.run(Request(uri = uri"/bad")).map(_.status),
            equalTo(Status.NotFound)
          )
        },
        testM("root request returns body 'hello!'") {
          for {
            response <- Hello1Service.service.run(Request())
            body <- response.bodyAsText.compile.toVector.map(_.mkString(""))
          } yield assert(body, equalTo("hello!"))
        }
      )
    )
