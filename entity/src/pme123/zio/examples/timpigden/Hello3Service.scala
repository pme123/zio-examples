package pme123.zio.examples.timpigden

import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.dsl.Http4sDsl
import zio.Task
import zio.interop.catz._
import XmlEncoders._
import Person._

object Hello3Service {

  private val dsl = Http4sDsl[Task]
  import dsl._

  val service = HttpRoutes.of[Task] {
    case GET -> Root => Ok("hello3!")
    case GET -> Root / "president" => Ok(Person.donald) // uses implicit encoder
    case req @ POST -> Root / "ageOf" =>
      req.decode[Person] { m => Ok(m.age.toString)}
  }.orNotFound
}
