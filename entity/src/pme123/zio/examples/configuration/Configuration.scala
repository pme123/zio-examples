package pme123.zio.examples.configuration

import pureconfig.loadConfigOrThrow
import pureconfig.generic.auto._

import zio.{RIO, Task}

trait Configuration extends Serializable {
  val config: Configuration.Service[Any]
}

object Configuration {

  trait Service[R] {
    val load: RIO[R, Config]
  }

  trait Live extends Configuration {
    final val config: Service[Any] = new Service[Any] {
      final val load: Task[Config] =
        Task.effect(
          loadConfigOrThrow[Config]
        )
    }
  }

  object Live extends Live
}
