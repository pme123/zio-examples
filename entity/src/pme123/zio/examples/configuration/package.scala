package pme123.zio.examples

import zio.{RIO, ZIO}

package object configuration extends Configuration.Service[Configuration] {

  final val configService: ZIO[Configuration, Nothing, Configuration.Service[Any]] =
    ZIO.access(_.config)

  final val load: RIO[Configuration, Config] =
    ZIO.accessM(_.config.load)

  case class Config(api: ApiConfig, dbConfig: DbConfig, swapi: SwapiConfig) {
    def print: String =
      copy(dbConfig = dbConfig.printed).toString
  }

  case class ApiConfig(endpoint: String, port: Int)

  case class DbConfig(url: String, user: String, password: String){
    def printed: DbConfig =
      copy(password = "*" * password.length )
  }

  case class SwapiConfig(url: String)

}

