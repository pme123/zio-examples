package pme123.zio.examples

import org.scalatest.{Matchers, WordSpec}
import pme123.zio.examples.configuration.Configuration._
import pme123.zio.examples.configuration._
import zio.{DefaultRuntime, Ref}


class ConfigurationTest extends WordSpec with Matchers {

  "The configuration" should {
    val configs = Config(
      ApiConfig("localhost", 9999),
      DbConfig("localhost", "sa", "myPassword"),
      SwapiConfig("https://swapi.co/api")
    )

    "be loaded" in {
      new DefaultRuntime {}.unsafeRun(
        for {
          state <- Ref.make(configs)
          result <- Configuration.>.load.provide(Test(state))
        } yield result
      ) shouldBe configs
    }
  }
}
