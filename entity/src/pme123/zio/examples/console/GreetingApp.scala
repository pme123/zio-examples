package pme123.zio.examples.console

import zio._

object GreetingApp extends App with Greeting {

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    program
      .provide(Console.Live)
      .fold(_ => 1, _ => 0)

  private lazy val program: ZIO[Console, Throwable, Unit] = for {
    _ <- greeting
  } yield ()
}
