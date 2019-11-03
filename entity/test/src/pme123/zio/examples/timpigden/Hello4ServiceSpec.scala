package pme123.zio.examples.timpigden

import org.http4s._
import org.http4s.implicits._
import pme123.zio.examples.timpigden.Middlewares._
import pme123.zio.examples.timpigden.XmlEncoders._
import zio.interop.catz._
import zio.test.Assertion._
import zio.test.{testM, _}

object Hello4ServiceSpec
    extends DefaultRunnableSpec(
      suite("routes suites")(
        testM("persident is Donald") {
          val req = requestWithAuth(uri = uri"/president")
          val io = (for {
            response <- hello4Service.run(req)
            body <- response.bodyAsText.compile.toVector.map(_.mkString(""))
            parsed <- parseIO(body)
          } yield parsed)
            .provide(authenticator)
          assertM(io, equalTo(Person.donald))
        },
        testM("joe has the age of 76") {
          val req1 = requestWithAuth(method = Method.POST, uri = uri"/ageOf")
            .withEntity(Person.joe)
          val req =
            AuthenticationHeaders.addAuthentication(req1, "tim", "friend")

          val io = (for {
            response <- hello4Service.run(req)
            body <- response.bodyAsText.compile.toVector.map(_.mkString(""))
          } yield body)
            .provide(authenticator)
          assertM(io, equalTo("76"))
        }
      )
    )
