package pme123.zio.examples.ghostdogpr

import zio._
import zio.console.Console
import zio.stream.ZStream

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object TheOneWithTheLoop extends App {

  def numberProvider(implicit ec: ExecutionContext): Future[Seq[Int]] =
    Future {
      Thread.sleep(Random.nextInt(1000))
      val upperBound = Random.nextInt(20) + 10
      for {n <- 10 to upperBound} yield Random.nextInt(n)
    }

  def numberGenerator: ZStream[Console, Throwable, Int] =
    ZStream.fromEffect(
      for{
       _ <- console.putStrLn("*"*50)
        n <- Task.fromFuture(implicit ec => numberProvider)
      }yield n
    ).forever
    .flatMap(ZStream.fromIterable)

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    numberGenerator
    .foreach(n => console.putStrLn(s"$n"))
      .foldM(
        ex => console.putStrLn(s"ERROR: ${ex.toString}").as(-1),
        res => console.putStrLn(s"$res").as(0)
      )

}
