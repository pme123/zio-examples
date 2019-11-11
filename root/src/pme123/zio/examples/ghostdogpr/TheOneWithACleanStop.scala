package pme123.zio.examples.ghostdogpr

import zio._
import zio.blocking._

import scala.io.{Codec, Source}

object TheOneWithACleanStop extends App {

  def getResource(path: String): ZIO[Blocking, Throwable, String] =
    Managed
      .make(Task(Source.fromResource(path)(Codec.UTF8)))(
        src => Task.effect(src.close()).ignore
      )
      .use { res =>
        effectBlocking(res.getLines.mkString)
      }

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    getResource("./application.conf").foldM(
      ex => console.putStrLn(s"ERROR: ${ex.toString}").as(-1),
      res => console.putStrLn(res).as(0)
    )

}
