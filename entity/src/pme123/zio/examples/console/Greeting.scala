package pme123.zio.examples.console

import zio.RIO

trait Greeting {
  val greeting: RIO[Console, Unit] =
    for {
      _ <- println("Hello what is your name?")
      name <- readLine
      _ <- println(s"Nice to meet you $name")
    } yield ()
}
