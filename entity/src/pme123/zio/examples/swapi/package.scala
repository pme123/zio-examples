package pme123.zio.examples

import zio.{RIO, ZIO}

package object swapi {
  final val swapiService: ZIO[Swapi, Nothing, Swapi.Service[Any]] =
    ZIO.access(_.swapi)

  final def people(id: Int): RIO[Swapi, People] =
    ZIO.accessM(_.swapi.people(id))

  case class People(
                     name: String = "Luke Skywalker",
                     height: String = "172",
                     mass: String = "77",
                     hair_color: String = "blond",
                     skin_color: String = "fair",
                     eye_color: String = "blue",
                     birth_year: String = "19BBY",
                     gender: String = "male",
                     homeworld: String = "https://swapi.co/api/planets/1/",
                     films: Seq[String] = Seq(
                       "https://swapi.co/api/films/2/",
                       "https://swapi.co/api/films/6/",
                       "https://swapi.co/api/films/3/",
                       "https://swapi.co/api/films/1/",
                       "https://swapi.co/api/films/7/"
                     ),
                     species: Seq[String] = Seq(
                       "https://swapi.co/api/species/1/"
                     ),
                     vehicles: Seq[String] = Seq(
                       "https://swapi.co/api/vehicles/14/",
                       "https://swapi.co/api/vehicles/30/"
                     ),
                     starships: Seq[String] = Seq(
                       "https://swapi.co/api/starships/12/",
                       "https://swapi.co/api/starships/22/"
                     ),
                     created: String = "2014-12-09T13:50:51.644000Z",
                     edited: String = "2014-12-20T21:17:56.891000Z",
                     url: String = "https://swapi.co/api/people/1/"
                   )

  case class ServiceException(msg: String) extends Throwable
  case class DeserializeException(msg: String) extends Throwable

}
