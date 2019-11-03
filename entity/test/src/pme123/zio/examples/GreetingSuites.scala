package pme123.zio.examples

import pme123.zio.examples.console.Console.Test
import pme123.zio.examples.console._
import zio.Ref
import zio.test.Assertion._
import zio.test._

object GreetingTests {

  //noinspection TypeAnnotation
  lazy val testSuites = suite("The greeting") {
    testM("be handled correctly") {
      for {
        state <- Ref.make(Vector.empty[String])
        _ <- Greeting.greeting.provide(Test(state))
        result <- state.get
      } yield assert(
        result,
        equalTo(Vector("Hello what is your name?", "Nice to meet you Pascal"))
      )
    }
  }
}

object GreetingSuites
    extends DefaultRunnableSpec(
      suite("GreetingSuites")(
        GreetingTests.testSuites
      )
    )
