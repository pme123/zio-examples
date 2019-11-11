package pme123.zio.examples.ghostdogpr

import zio._

object TheOneThatCallsBack extends App {

  private def checkIsEven (
      input: Int,
      onSuccess: String => Unit,
      onFailure: Throwable => Unit
  ): Unit =
    if (input % 2 == 0)
      onSuccess(s"YES! $input is even")
    else
      onFailure(new IllegalArgumentException(s"$input is NOT even"))

  private def sendEven(input: Int) =
    IO.effectAsync[Throwable, Unit] { cb =>
      checkIsEven(input, msg => cb(IO.succeed(msg)), ex => cb(IO.fail(ex)))
    }

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      number <- random.nextInt
      _ <- sendEven(number)
    } yield ())
      .foldM(
        ex => console.putStrLn(s"ERROR: ${ex.toString}").as(-1),
        _ => console.putStrLn(s"Sending succeeded").as(0)
      )

}
