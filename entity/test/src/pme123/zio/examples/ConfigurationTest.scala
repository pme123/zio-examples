package pme123.zio.examples

import org.scalatest.{Matchers, WordSpec}
import pme123.zio.examples.configuration.Configuration._
import pme123.zio.examples.configuration._
import zio.{DefaultRuntime, Ref, Task}


class ConfigurationTest extends WordSpec with Matchers {

  case class Test(ref: Ref[Config]) extends Configuration {
    val config: Service[Any] = new Service[Any] {

      val load: Task[Config] = ref.get
    }
  }

  "The configuration" should {
    val configs = Config(
      ApiConfig("localhost", 9999),
      DbConfig("localhost", "sa", "myPassword"))

    "be loaded" in {
      new DefaultRuntime {}.unsafeRun(
        for {
          state <- Ref.make(configs)
          result <- load.provide(Test(state))
        } yield result
      ) shouldBe configs
    }
  }
}
