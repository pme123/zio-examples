package pme123.zio.examples.hocon

import com.typesafe.config.ConfigRenderOptions
import pureconfig.generic.auto._
import pureconfig.generic.semiauto._
import pureconfig.{ConfigReader, ConfigSource, ConfigWriter}
import zio._
import zio.console.Console

import scala.reflect.ClassTag

object HoconApp extends App {
  val dbLookupName = "postcodeLookup"
  val messageBundleName = "messageBundle.en"

  case class Sensitive(value: String) extends AnyVal {
    override def toString: String = "*" * 20
  }

  sealed trait Component {
    def name: String
    def fileName: String = s"$name.conf"
  }
  case class DbConnection(
      name: String,
      url: String,
      user: String,
      password: Sensitive
  ) extends Component

  case class DbLookup(
      name: String,
      dbConRef: CompRef,
      statement: String,
      params: Map[String, String]
  ) extends Component

  case class MessageBundle(
      name: String,
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

  implicit val componentReader: ConfigReader[Component] =
    deriveReader[Component]

  def loadConf[T <: Component: ConfigReader: ClassTag](
      ref: CompRef
  ): RIO[Console, T] = {
    for {
      component <- Task.effect(ConfigSource.resources(ref.url).loadOrThrow[T])
      _ <- console.putStrLn(s"\nComponent:\n$component")
    } yield component
  }

  def writeConf[T <: Component: ConfigWriter: ClassTag](
      component: T
  ): RIO[Console, String] =
    for {
      configValue <- ZIO.effectTotal(ConfigWriter[T].to(component))
      configString <- ZIO.effectTotal(configValue.render())
      _ <- console.putStrLn(
        s"\nComponent File ${component.fileName}:\n$configString"
      )
    } yield configString

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      dbLookup <- loadConf[DbLookup](LocalRef(dbLookupName))
      dbConnection <- loadConf[DbConnection](dbLookup.dbConRef)
      messageBundle <- loadConf(LocalRef(messageBundleName))
      _ <- writeConf(dbLookup)
      _ <- writeConf(dbConnection)
      _ <- writeConf(messageBundle)
    } yield 0)
      .catchAll { x =>
        console.putStrLn(s"Exception: $x") *>
          UIO.effectTotal(1)
      }

}
