package pme123.zio.examples

import org.scalatest.{Matchers, WordSpec}
import pme123.zio.examples.console.Console.Test
import pme123.zio.examples.console._
import zio.{DefaultRuntime, Ref}


class GreetingTest
  extends WordSpec
    with Matchers
    with Greeting {

  "The greeting" should {
    "be handled correctly" in {
      new DefaultRuntime {}.unsafeRun(
        for {
          state <- Ref.make(Vector.empty[String])
          _ <- greeting.provide(Test(state))
          result <- state.get
        } yield result
      ) shouldBe Vector("Hello what is your name?", "Nice to meet you Pascal")
    }
  }
}

