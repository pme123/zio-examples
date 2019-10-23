package pme123.zio.examples.basics

import zio.console._
import zio.duration._
import zio.{App, Schedule, ZIO}

object SchedulerApp extends App {

  def run(args: List[String]): ZIO[Environment, Nothing, Int] =
    program
      .fold({ error =>
        error.printStackTrace()
        1
      }, _ => 0)

  val s = Schedule.spaced(1.second)
  private lazy val program: ZIO[Environment, Throwable, Unit] = for {
    _ <- putStrLn("Start")
    _ <- putStrLn("Initial Delay").delay(5.seconds)
    _ <- putStrLn("Repeated Delay").repeat(s)
  } yield ()


  //surpressed
  def d(o: Option[String]): String =
    o match {
      case None => "none"
    }
}
