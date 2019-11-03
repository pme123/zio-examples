package pme123.zio.examples

package object configuration {

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

