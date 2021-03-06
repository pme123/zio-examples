package pme123.zio.examples.configuration

import pureconfig.loadConfigOrThrow
import pureconfig.generic.auto._
import zio.{RIO, Ref, Task, ZIO}

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

  object > extends Service[Configuration] {
    final val load: RIO[Configuration, Config] =
      ZIO.accessM(_.config.load)
  }

  case class Test(ref: Ref[Config]) extends Configuration {
    val config: Service[Any] = new Service[Any] {

      val load: Task[Config] = ref.get
    }
  }
}
