package pme123.zio.examples.xmodule

import zio.ZIO
import pureconfig._
import pureconfig.generic.auto._

trait ConfigModule {
  val configModule: ConfigModule.Service[Any]
}

object ConfigModule {

  case class ConfigException(msg: String) extends RuntimeException(msg)
  case class Config(appName: String)
  trait Service[R] {
    def config: ZIO[R, ConfigException, Config]
  }

  object > extends Service[ConfigModule] {
    def config: ZIO[ConfigModule, ConfigException, Config] =
      ZIO.accessM(_.configModule.config)
  }

  trait Live extends ConfigModule {
    val configModule: Service[Any] = new Service[Any] {
      def config: ZIO[Any, ConfigException, Config] =
        ZIO
          .fromEither(ConfigSource.default.load[Config])
          .mapError(e => ConfigException(e.toList.mkString("\n")))
    }
  }
}
