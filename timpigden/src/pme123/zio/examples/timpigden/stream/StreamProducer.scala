package pme123.zio.examples.timpigden.stream

import zio.clock.Clock
import zio.console.Console.Live
import zio.duration._
import zio.stream.ZStream
import zio.{ZIO, ZSchedule}

object StreamProducer {

  def stream: ZStream[Clock, Nothing, SimpleEvent] =
    ZStream
      .repeatEffect(
        for {
          at <- ZIO.accessM[Clock](_.clock.currentDateTime)
          evt = SimpleEvent(at.toInstant)
          _ <- Live.console.putStrLn(s"at $evt")
        } yield evt
      )
      .schedule(ZSchedule.spaced(10.seconds))

}
