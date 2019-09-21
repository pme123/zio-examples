package pme123.zio.examples.configuration
import pme123.zio.examples.console._

import zio.{App, ZIO}

object ConfigurationApp
  extends App {

  def run(args: List[String]): ZIO[Environment, Nothing, Int] =
    program.fold({error =>
      error.printStackTrace()
      1}, _ => 0)

  private lazy val program = for {
    configs <- load.provide(Configuration.Live)
    _ <- println(s"Config is: ${configs.print}").provide(Console.Live)
  } yield ()
}

