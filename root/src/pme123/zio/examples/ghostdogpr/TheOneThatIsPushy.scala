package pme123.zio.examples.ghostdogpr

import zio._
import zio.blocking.effectBlocking
import zio.stream.ZStream

import scala.util.Random

object TheOneThatIsPushy extends App {

  def numberProvider(queue: Queue[Int], rts: Runtime[Any]) =
    ZStream
      .fromEffect {
        effectBlocking {
          Thread.sleep(Random.nextInt(100))
          rts.unsafeRun(queue.offer(Random.nextInt(100)))
        }
      }
      .forever
      .foreach(_ => ZIO.unit)

  def numberConsumer(queue: Queue[Int]) =
    ZStream.fromQueue(queue).forever

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      queue <- Queue.bounded[Int](1000)
      rts <- Task.runtime
      _ <- numberProvider(queue, rts).fork
      _ <- numberConsumer(queue).zipWithIndex
        .foreach(n => console.putStrLn(s"$n"))
    } yield ())
      .catchAll(ex => console.putStrLn(s"ERROR: ${ex.toString}"))
      .fold(
        _ => -1,
        _ => 0
      )

}
