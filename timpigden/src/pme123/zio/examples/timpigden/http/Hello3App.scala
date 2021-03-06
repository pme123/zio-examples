package pme123.zio.examples.timpigden.http

import org.http4s.server.blaze.BlazeServerBuilder
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._

object Hello3App extends App {

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    program
      .fold(ex => {
        ex.printStackTrace()
        1
      }, _ => 0)

  def program: ZIO[ZEnv, Throwable, Unit] =
    ZIO
      .runtime[ZEnv]
      .flatMap { implicit env =>
        BlazeServerBuilder[Task]
          .bindHttp(9009, "localhost")
          .withHttpApp(Hello3Service.service)
          .serve
          .compile
          .drain
      }
}
