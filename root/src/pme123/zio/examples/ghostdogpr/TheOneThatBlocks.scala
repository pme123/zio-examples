package pme123.zio.examples.ghostdogpr

import zio._
import zio.blocking._

import scala.io.{Codec, Source}

object TheOneThatBlocks extends App {

  def getResource(path: String): ZIO[Blocking, Throwable, String] =
    effectBlocking {
      Source.fromResource(path)(Codec.UTF8).getLines.mkString
    }

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    getResource("./application.conf").foldM(
      ex => console.putStrLn(s"ERROR: ${ex.toString}").as(-1),
      res => console.putStrLn(res).as(0)
    )

}
