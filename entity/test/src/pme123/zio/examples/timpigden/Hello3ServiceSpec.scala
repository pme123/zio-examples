package pme123.zio.examples.timpigden

import org.http4s._
import org.http4s.implicits._
import pme123.zio.examples.timpigden.XmlEncoders._
import zio.Task
import zio.interop.catz._
import zio.test.Assertion._
import zio.test.{testM, _}

object Hello3ServiceSpec
    extends DefaultRunnableSpec(
      suite("routes suites")(
        testM("persident is Donald") {
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
