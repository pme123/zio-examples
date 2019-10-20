package pme123.zio.examples.swapi

import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.zio.AsyncHttpClientZioBackend
import com.softwaremill.sttp.circe._
import io.circe.generic.auto._
import pme123.zio.examples.configuration.SwapiConfig
import zio.{RIO, Ref, Task}


trait Swapi extends Serializable {
  val swapi: Swapi.Service[Any]
}

object Swapi {

  trait Service[R] {
    def people(id:Int): RIO[R, People]
  }

  trait Live extends Swapi {

    //noinspection TypeAnnotation
    private implicit val sttpBackend = AsyncHttpClientZioBackend()

    protected def config: SwapiConfig

    private lazy val url = config.url

    override val swapi: Service[Any] = new Service[Any] {

      final def people(id:Int): Task[People] = {
        sttp
          .get(uri"""$url/people/$id/""")
          .response(asJson[People])
          .send()
          .map(_.body)
          .map {
            case Left(msg) =>
              throw ServiceException(msg)
            case Right(Left(err)) => throw ServiceException(err.message)
            case Right(Right(value)) => value
          }
      }

    }
  }

  case class Test(ref: Ref[Vector[People]]) extends Swapi {
    override val swapi: Swapi.Service[Any] = new Swapi.Service[Any] {
      def people(id: Int): RIO[Any, People] =
        for{
          peoples <- ref.get
          maybePeople = peoples.find(_.url.contains(s"/people/$id"))
        }yield maybePeople match {
          case None =>
            throw ServiceException(s"No People with id $id")
          case Some(p) => p
        }
    }
  }
}
