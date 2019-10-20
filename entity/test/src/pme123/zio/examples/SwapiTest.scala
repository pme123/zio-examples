package pme123.zio.examples

import org.scalatest.{Matchers, WordSpec}
import pme123.zio.examples.swapi.Swapi.Test
import pme123.zio.examples.swapi.{People, ServiceException, people}
import zio._

class SwapiTest
  extends WordSpec
    with Matchers {

  "The SWAPI" should {
    "get Luke Skywalker" in {
      new DefaultRuntime {}.unsafeRun(
        (for {
          peopleRef <- Ref.make(Vector(People()))
          luke <- people(1).provide(Test(peopleRef))
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
          _ <- people(2).provide(Test(peopleRef))
        } yield ())
          .fold(
            err => err shouldBe ServiceException("No People with id 2"),
            _ => fail("line above should fail")
          )
      )
    }
  }
}
