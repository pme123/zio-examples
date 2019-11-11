package pme123.zio.examples.ghostdogpr

import zio._

import scala.concurrent.{ExecutionContext, Future}

object TheOneLivingInTheFuture extends App {

  private def send(msg: String)(implicit ec: ExecutionContext): Future[String] =
    Future {
      Thread.sleep(100)
      s"Message '$msg' sent..."
    }

  private def sendMsg(msg: String) =
    IO.fromFuture { implicit ec =>
      send(msg)
    }

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    sendMsg("hello there")
      .foldM(
        ex => console.putStrLn(s"ERROR: ${ex.toString}").as(-1),
        msg => console.putStrLn(msg).as(0)
      )

}
