package pme123.zio.examples.swapi

import pme123.zio.examples.configuration.{Configuration, SwapiConfig}
import pme123.zio.examples.console.Console
import pme123.zio.examples.{configuration, console}
import zio._
import zio.duration._

object SwapiApp
  extends App {

  def run(args: List[String]): ZIO[Environment, Nothing, Int] =
    program
      .fold({ error =>
        error.printStackTrace()
        1
      }, _ => 0)
  val s = Schedule.once.delayed(_ + 5.seconds)
  val s2 = Schedule.spaced(1.second).forever
s *> s2
  private lazy val program: ZIO[Environment, Throwable, Unit] = for {
    console <- console.consoleService.provide(Console.Live)
    _ <- console.println("Start")
    _ <- console.println("Initial Delay").delay(5.seconds)
    _ <- console.println("Initial Delay").repeat(s2)

    conf <- configuration.load.provide(Configuration.Live)
    server = ZIO.runtime[Swapi].provideSome[Environment](_ =>
      new Swapi.Live {
        protected def config: SwapiConfig = conf.swapi
      }
    )

    service <- swapiService.provide(new Swapi.Live {
      protected def config: SwapiConfig = conf.swapi
    })
    // do stuff
    luke <- service.people(1)
    _ <- console.println(luke.name)
    _ <- service.people(99999) // this throws an exception
  } yield ()
}
