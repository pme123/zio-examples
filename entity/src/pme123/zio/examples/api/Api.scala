package pme123.zio.examples.api

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import pme123.zio.examples.persistence._
import zio.RIO
import zio.interop.catz._


final case class Api[R <: Persistence] (rootUri: String){
  type UserTask[A] = RIO[R, A]
  val dsl: Http4sDsl[UserTask] = Http4sDsl[UserTask]

  import dsl._

  def routes: HttpRoutes[UserTask] =
    HttpRoutes.of[UserTask] {
      case GET -> Root =>
        all().foldM(_ => NotFound(), Ok(_))
      case GET -> Root / IntVar(id) =>
        get(id).foldM(_ => NotFound(), Ok(_))
      case request@POST -> Root =>
        request.decode[User] { user =>
          Created(create(user))
        }
      case DELETE -> Root / IntVar(id) =>
        (get(id) *> delete(id)).foldM(_ => NotFound(), Ok(_))
    }

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[UserTask, A] = jsonOf[UserTask, A]

  implicit def circeJsonEncoder[A](implicit decoder: Encoder[A]): EntityEncoder[UserTask, A] =
    jsonEncoderOf[UserTask, A]
}
