package pme123.zio.examples

import org.scalatest.{Matchers, WordSpec}
import pme123.zio.examples.console._
import zio.{DefaultRuntime, Ref, UIO}


class GreetingTest
  extends WordSpec
    with Matchers
    with Greeting {

  case class Test(ref: Ref[Vector[String]]) extends Console {
    override val console: Console.Service[Any] = new Console.Service[Any] {
      final def println(line: String): UIO[Unit] =
        ref.update(_ :+ line).unit

      final val readLine: UIO[String] =
        UIO("Pascal")
    }
  }

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

