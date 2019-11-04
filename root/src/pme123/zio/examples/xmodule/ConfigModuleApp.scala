package pme123.zio.examples.xmodule

import pme123.zio.examples.xmodule.XModule.X
import zio._
import zio.console.Console

object ConfigModuleApp extends App {

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = program

  private def program =
    logic
      .provideSome[Console] { c =>
        new Console with ConfigModule.Live {
          val console: Console.Service[Any] = c.console
        }
      }

  private val logic: ZIO[Console with ConfigModule, Nothing, Int] =
    (for {
      _ <- console.putStrLn(s"I'm running!")
      config <- ConfigModule.>.config
      _ <- console.putStrLn(s"This is the '${config.appName}' application")
    } yield 0)
      .catchAll(e => console.putStrLn(s"Application run failed $e").as(1))
}
