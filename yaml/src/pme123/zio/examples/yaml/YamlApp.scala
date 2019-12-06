package pme123.zio.examples.yaml

import cats.syntax.functor._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.yaml.parser
import io.circe.yaml.syntax._
import io.circe.{Decoder, Encoder, Json, ParsingFailure}
import pme123.zio.examples.hocon._
import zio._
import zio.console.Console

import scala.io.Source

object YamlApp extends App {

  implicit val sensitive: Decoder[Sensitive] =
    Decoder[String].map(Sensitive).widen

  implicit val decodeComponent: Decoder[Component] =
    List[Decoder[Component]](
      Decoder[DbConnection].widen,
      Decoder[DbLookup].widen,
      Decoder[MessageBundle].widen
    ).reduceLeft(_ or _)

  implicit val decodeCompRef: Decoder[CompRef] =
    List[Decoder[CompRef]](
      Decoder[LocalRef].widen,
      Decoder[RemoteRef].widen
    ).reduceLeft(_ or _)

  def loadYaml[T <: Component: Decoder](ref: CompRef): RIO[Console, T] = {
    val yamlString = Source.fromResource(s"${ref.url}.yaml").mkString
    val json: Either[ParsingFailure, Json] = parser.parse(yamlString)
    for {
      _ <- console.putStrLn(s"\nJSON:\n$json")
      comp = json.flatMap(_.as[T])
      component <- Task.fromEither(comp)
      _ <- zio.console.putStrLn(s"\nComponent:\n$component")
    } yield component
  }
  def writeYaml[T <: Component: Encoder](
      component: T
  ): RIO[Console, String] =
    for {
      json <- ZIO.effectTotal(component.asJson)
      configString <- ZIO.effectTotal(json.asYaml.spaces2)
      _ <- console.putStrLn(
        s"\nComponent File ${component.name}.conf :\n$configString"
      )
    } yield configString

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      dbLookup <- loadYaml[DbLookup](LocalRef(dbLookupName))
      dbConnection <- loadYaml[DbConnection](dbLookup.dbConRef)
      messageBundle <- loadYaml(LocalRef(messageBundleName))
      _ <- writeYaml(dbLookup)
      _ <- writeYaml(dbConnection)
      _ <- writeYaml(messageBundle)
    } yield 0)
      .catchAll { x =>
        console.putStrLn(s"Exception: $x") *>
          UIO.effectTotal(1)
      }

}
