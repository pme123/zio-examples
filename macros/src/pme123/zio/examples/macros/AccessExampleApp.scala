package pme123.zio.examples.macros

import zio.{App, ZIO}
import zio.Schedule

object AccessExampleApp extends App {

  def run(args: List[String]): ZIO[Environment, Nothing, Int] =
    myProgram
      .fold({ error =>
        // error.printStackTrace()
        1
      }, _ => 0)

  val myProgram =
    for {
      _ <- AccessExample.>.foo.provide(AccessExample.Live)
      _ <- AccessExample.>.bar(1, 2).provide(AccessExample.Live)
      _ <- AccessExample.>.baz(1)(2).provide(AccessExample.Live)
    } yield ()


}
