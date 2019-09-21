package pme123.zio.examples.console

import zio.{App, ZIO}

object GreetingApp
  extends App
    with Greeting {
  def run(args: List[String]): ZIO[Environment, Nothing, Int] =
    program.fold(_ => 1, _ => 0)

  private lazy val program = for {
    _ <- greeting.provide(Console.Live)
  } yield ()
}

