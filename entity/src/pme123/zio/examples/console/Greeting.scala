package pme123.zio.examples.console

import zio.RIO

trait Greeting {
  val greeting: RIO[Console, Unit] =
    for {
      _ <- Console.>.println("Hello what is your name?")
      name <- Console.>.readLine
      _ <- Console.>.println(s"Nice to meet you $name")
    } yield ()
}

object Greeting extends Greeting
