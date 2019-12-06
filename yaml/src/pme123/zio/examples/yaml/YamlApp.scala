package pme123.zio.examples.yaml

import cats.syntax.functor._
import io.circe.generic.auto._
import io.circe.yaml.parser
import io.circe.{Decoder, Json, ParsingFailure}
import zio.console.Console
import zio._

import scala.io.Source

object YamlApp extends App {
  val dbLookupName = "dbLookup"
  val messageBundleName = "messageBundle.en"

  sealed trait Component {}
  case class DbConnection(
      name: String,
      url: String,
      user: String,
      password: String
  ) extends Component

  case class DbLookup(
      name: String,
      dbConRef: CompRef,
      statement: String,
      params: Map[String, String]
  ) extends Component

  case class MessageBundle(
      params: Map[String, String]
  ) extends Component

  sealed trait CompRef {
    def url: String
  }
  case class LocalRef(name: String) extends CompRef {
    val url = s"$name.yaml"
  }
  case class RemoteRef(name: String, pckg: String) extends CompRef {
    val url = s"dependencies/$pckg/$name.yaml"
  }

  implicit val decodeComponent: Decoder[Component] =
    List[Decoder[Component]](
      Decoder[DbConnection].widen,
      Decoder[DbLookup].widen,
      Decoder[Map[String, String]].map(MessageBundle).widen
    ).reduceLeft(_ or _)

  implicit val decodeCompRef: Decoder[CompRef] =
    List[Decoder[CompRef]](
      Decoder[LocalRef].widen,
      Decoder[RemoteRef].widen
    ).reduceLeft(_ or _)

  def loadYaml[T <: Component: Decoder](ref: CompRef): RIO[Console, T] = {
    val yamlString = Source.fromResource(ref.url).mkString
    val json: Either[ParsingFailure, Json] = parser.parse(yamlString)
    for {
      _ <- console.putStrLn(s"\nJSON:\n${json.toString}")
      comp = json.flatMap(_.as[T])
      component <- Task.fromEither(comp)
      _ <- zio.console.putStrLn(s"\nComponent:\n${component.toString}")
    } yield component
  }

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      dbLookup <- loadYaml[DbLookup](LocalRef(dbLookupName))
      _ <- loadYaml[DbConnection](dbLookup.dbConRef)
      _ <- loadYaml(LocalRef(messageBundleName))
    } yield 0)
      .catchAll { x =>
        console.putStrLn(s"Exception: $x") *>
          UIO.effectTotal(1)
      }

}
