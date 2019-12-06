package pme123.zio.examples.hocon

import pureconfig.generic.auto._
import pureconfig.generic.semiauto._
import pureconfig.{ConfigReader, ConfigSource, ConfigWriter}
import zio._
import zio.console.Console

import scala.reflect.ClassTag

object HoconApp extends App {

  implicit val componentReader: ConfigReader[Component] = deriveReader[Component]

  def loadConf[T <: Component: ConfigReader: ClassTag](
      ref: CompRef
  ): RIO[Console, T] = {
    for {
      component <- Task.effect(ConfigSource.resources(s"${ref.url}.conf").loadOrThrow[T])
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
        s"\nComponent File ${component.name}.conf :\n$configString"
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
