package pme123.zio.examples.swapi

import pme123.zio.examples.configuration.{Configuration, SwapiConfig}
import pme123.zio.examples.console.Console
import zio._

object SwapiApp
  extends App {

  def run(args: List[String]): ZIO[Environment, Nothing, Int] =
    program
      .fold({ error =>
        error.printStackTrace()
        1
      }, _ => 0)

  private lazy val program = for {
    console <- Console.>.console.provide(Console.Live)
    _ <- console.println("Start")
    conf <- Configuration.>.load.provide(Configuration.Live)
    swapiEnv = new Swapi.Live {
      protected def swapiConfig: SwapiConfig = conf.swapi
    }
    // do stuff
    luke <- Swapi.>.people(1).provide(swapiEnv)
    _ <- console.println(luke.name)
    _ <- Swapi.>.people(99999).provide(swapiEnv).catchAll { ex =>
      console.println("The expected error happend")
          .flatMap(_ => ZIO.fail(ex))
    }
  } yield ()
}
