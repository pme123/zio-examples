package pme123.zio.examples

import pme123.zio.examples.configuration.Configuration._
import pme123.zio.examples.configuration._
import zio.Ref
import zio.test.Assertion.equalTo
import zio.test.{DefaultRunnableSpec, assert, suite, testM}

object ConfigurationTests {
  val configs = Config(
    ApiConfig("localhost", 9999),
    DbConfig("localhost", "sa", "myPassword"),
    SwapiConfig("https://swapi.co/api")
  )

  lazy val testSuites = suite("ConfigurationTestSuites") {
    suite("The configuration")(
      testM("be loaded") {
        for {
          state <- Ref.make(configs)
          result <- Configuration.>.load.provide(Test(state))
        } yield assert(result, equalTo(configs))
      }
    )
  }
  lazy val liveSuites = suite("ConfigurationLiveSuites") {
    suite("The configuration")(
      testM("be loaded") {
        for {
          state <- Ref.make(configs)
          result <- Configuration.>.load.provide(Test(state))
        } yield assert(result, equalTo(configs))
      }
    )
  }
}

object ConfigurationSuites
    extends DefaultRunnableSpec(
      suite("SwapiSuites")(
        ConfigurationTests.testSuites,
        ConfigurationTests.liveSuites
      )
    )
