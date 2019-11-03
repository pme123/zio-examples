package pme123.zio.examples.macros

import zio._
import zio.console._
object AccessExampleApp extends App {

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    myProgram
      .fold({ error =>
         putStrLn(s"Error: $error")
        1
      }, _ => 0)

  val myProgram =
    for {
      _ <- AccessExample.>.foo.provide(AccessExample.Live)
      _ <- AccessExample.>.bar(1, 2).provide(AccessExample.Live)
      _ <- AccessExample.>.baz(1)(2).provide(AccessExample.Live)
    } yield ()


}
