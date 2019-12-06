package pme123.zio.examples.hocon

import pureconfig.generic.ProductHint
import pureconfig.generic.auto._
import pureconfig.generic.semiauto._
import pureconfig.{CamelCase, ConfigFieldMapping, ConfigReader, ConfigSource}
import zio._
import zio.console.Console

import scala.reflect.ClassTag

object HoconApp extends App {
  val dbLookupName = "postcodeLookup"
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
      name:String,
      params: Map[String, String]
  ) extends Component

  sealed trait CompRef {
    def url: String
  }
  case class LocalRef(name: String) extends CompRef {
    val url = s"$name.conf"
  }
  case class RemoteRef(name: String, pckg: String) extends CompRef {
    val url = s"dependencies/$pckg/$name.conf"
  }

  implicit val componentReader: ConfigReader[Component] = deriveReader[Component]

  def loadConf[T <: Component: ConfigReader : ClassTag](ref: CompRef): RIO[Console, T] = {
    for {
      component <- Task.effect(ConfigSource.resources(ref.url).loadOrThrow[T])
      _ <- zio.console.putStrLn(s"\nComponent:\n$component")
    } yield component
  }

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      dbLookup <- loadConf[DbLookup](LocalRef(dbLookupName))
      _ <- loadConf[DbConnection](dbLookup.dbConRef)
      _ <- loadConf(LocalRef(messageBundleName))
    } yield 0)
      .catchAll { x =>
        console.putStrLn(s"Exception: $x") *>
          UIO.effectTotal(1)
      }

}
