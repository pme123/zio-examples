package pme123.zio.examples.configuration
import pme123.zio.examples.console._

import zio._

object ConfigurationApp extends App {
  type MyEnv = Configuration with Console
  val env = new Configuration.Live with Console.Live

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    program.fold(
      _ => 1,
      _ => 0
    )

  private lazy val program =
    logic
      .provide {
        new Configuration.Live with Console.Live
      }

  private val logic: ZIO[Console with Configuration, Throwable, Unit] =
    (for {
      configs <- Configuration.>.load
      _ <- Console.>.println(s"Config is: ${configs.print}")
    } yield ())
      .catchAll(e => Console.>.println(s"Application run failed $e"))


}
