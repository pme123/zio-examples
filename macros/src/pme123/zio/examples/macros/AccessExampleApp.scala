package pme123.zio.examples.macros

import zio._
import zio.console._

package object module extends AccessExample.Accessors

object AccessExampleApp extends App {

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    myProgram
      .fold({ error =>
         putStrLn(s"Error: $error")
        1
      }, _ => 0)

  val myProgram =
    for {
      _ <- module.foo.provide(AccessExample.Live)
      _ <- module.bar(1, 2).provide(AccessExample.Live)
      _ <- module.baz(1)(2).provide(AccessExample.Live)
    } yield ()


}
