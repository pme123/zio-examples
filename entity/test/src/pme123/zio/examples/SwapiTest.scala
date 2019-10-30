package pme123.zio.examples

import org.scalatest.{Matchers, WordSpec}
import pme123.zio.examples.swapi.Swapi.Test
import pme123.zio.examples.swapi.{People, ServiceException, Swapi}
import zio._

class SwapiTest extends WordSpec with Matchers {

  "The SWAPI" should {
    "get Luke Skywalker" in {
      new DefaultRuntime {}.unsafeRun(
        (for {
          peopleRef <- Ref.make(Vector(People()))
          luke <- Swapi.>.people(1).provide(Test(peopleRef))
        } yield luke)
          .fold(
            _ => fail("No exception epected"),
            luke => luke shouldBe (People())
          )
      )
    }
    "throw Exception for other" in {
      new DefaultRuntime {}.unsafeRun(
        (for {
          peopleRef <- Ref.make(Vector(People()))
          _ <- Swapi.>.people(2).provide(Test(peopleRef))
        } yield ())
          .fold(
            err => err shouldBe ServiceException("No People with id 2"),
            _ => fail("line above should fail")
          )
      )
    }
  }
}

/*
object SwapiSuites
    extends DefaultRunnableSpec(suite("SwapiSuites") {


      suite("Get a Person for an ID") {
        testM("get Luke Skywalker") {
          for {
            peopleRef <- Ref.make(Vector(People()))
            luke <- Swapi.>.people(1).provide(Test(peopleRef))
          } yield assert(luke, equalTo(People()))
      }}
    })
*/