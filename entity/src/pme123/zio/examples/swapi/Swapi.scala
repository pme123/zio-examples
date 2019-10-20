package pme123.zio.examples.swapi

import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.zio.AsyncHttpClientZioBackend
import com.softwaremill.sttp.circe._
import io.circe.generic.auto._
import pme123.zio.examples.configuration.SwapiConfig
import zio.{IO, RIO, Ref, Task, ZIO}


trait Swapi extends Serializable {
  val swapi: Swapi.Service[Any]
}

object Swapi {

  trait Service[R] {
    def people(id: Int): RIO[R, People]
  }

  trait Live extends Swapi {

    //noinspection TypeAnnotation
    private implicit val sttpBackend = AsyncHttpClientZioBackend()

    protected def config: SwapiConfig

    private lazy val url = config.url

    override val swapi: Service[Any] = new Service[Any] {

      final def people(id: Int): Task[People] = {
        sttp
          .get(uri"""$url/people/$id/""")
          .response(asJson[People])
          .send()
          .map(_.body)
          .flatMap {
            case Left(msg) => IO.fail(ServiceException(msg))
            case Right(Left(err)) => IO.fail(DeserializeException(err.message))
            case Right(Right(value)) => ZIO.effectTotal(value)
          }
      }

    }
  }

  case class Test(ref: Ref[Vector[People]]) extends Swapi {
    override val swapi: Swapi.Service[Any] = new Swapi.Service[Any] {
      def people(id: Int): RIO[Any, People] =
        for {
          peoples <- ref.get
          result <- peoples.find(_.url.contains(s"/people/$id")) match {
            case None =>
              IO.fail(ServiceException(s"No People with id $id"))
            case Some(p) => ZIO.effectTotal(p)
          }
        } yield result
    }
  }

}
