package pme123.zio.examples.timpigden.http

import org.http4s.implicits._
import org.http4s.{Method, Request}
import pme123.zio.examples.timpigden.http.XmlEncoders.{parseIO, _}
import zio.Task
import zio.interop.catz._
import zio.test.Assertion.equalTo
import zio.test.{DefaultRunnableSpec, assert, suite, testM}

object Hello3ServiceSpec
    extends DefaultRunnableSpec(
      suite("Hello3ServiceSpec routes suites")(
        testM("president is Donald") {
          for {
            response <- Hello3Service.service.run(Request(Method.GET, uri"/president"))
            body <- response.bodyAsText.compile.toVector.map(_.mkString(""))
            parsed <- parseIO(body)
          } yield assert(parsed, equalTo(Person.donald))
        },
        testM("joe has the age of 76") {
          val req = Request[Task](Method.POST, uri"/ageOf")
            .withEntity(Person.joe)
          for {
            response <- Hello3Service.service.run(req)
            body <- response.bodyAsText.compile.toVector.map(_.mkString(""))
          } yield assert(body, equalTo("76"))
        }
      )
    )
