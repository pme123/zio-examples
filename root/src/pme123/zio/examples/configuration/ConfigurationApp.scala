package pme123.zio.examples.configuration
import pme123.zio.examples.console._

import zio._

object ConfigurationApp extends App {
  type MyEnv = Configuration with Console
  val env = new Configuration.Live with Console.Live
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    program
      .provide(env)
      .fold({ error =>
        error.printStackTrace()
        1
      }, _ => 0)

  private lazy val program: ZIO[MyEnv, Throwable, Unit] = for {
    configs <- Configuration.>.load
    _ <- Console.>.println(s"Config is: ${configs.print}")
  } yield ()
}
