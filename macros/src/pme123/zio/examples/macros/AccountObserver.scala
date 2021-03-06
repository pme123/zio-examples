package pme123.zio.examples.macros

import zio.{UIO, ZIO}
import zio.console.Console
import zio.macros.annotation.accessible

@accessible
trait AccountObserver {
  val accountObserver: AccountObserver.Service[Any]
}

object AccountObserver {
  trait Service[R] {
    def processEvent(event: String): ZIO[R, Nothing, Unit]
  }

  // autogenerated `object Service { ... }`
  // autogenerated `object > extends Service[AccountObserver] { ... }`
  // autogenerated `implicit val mockable: Mockable[AccountObserver] = ...`
}

trait AccountObserverLive extends AccountObserver {
  // dependency on Console module
  val console: Console.Service[Any]

  val accountObserver = new AccountObserver.Service[Any] {
    def processEvent(event: String): UIO[Unit] =
      for {
        _    <- console.putStrLn(s"Got $event")
        line <- console.getStrLn.orDie
        _    <- console.putStrLn(s"You entered: $line")
      } yield ()
  }
}
