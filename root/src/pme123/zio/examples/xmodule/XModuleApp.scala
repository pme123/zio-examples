package pme123.zio.examples.xmodule

import pme123.zio.examples.xmodule.XModule.X
import zio._
import zio.console.Console

object XModuleApp extends App {

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = program

  private def program =
    logic
      .provideSome[Console] { c =>
        new Console with XModule.Live {
          val console: Console.Service[Any] = c.console

          val xInstance: XModule.X = X()
        }
      }

  private val logic: ZIO[Console with XModule, Nothing, Int] =
    (for {
      _ <- console.putStrLn(s"I'm running!")
      x <- XModule.>.x
      _ <- console.putStrLn(s"I've got an $x!")
    } yield 0)
      .catchAll(e => console.putStrLn(s"Application run failed $e").as(1))
}
