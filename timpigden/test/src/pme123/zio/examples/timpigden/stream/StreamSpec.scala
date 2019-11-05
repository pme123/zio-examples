package pme123.zio.examples.timpigden.stream

import zio.ZSchedule
import zio.duration._
import zio.stream.Sink
import zio.test.Assertion._
import zio.test._
import zio.test.environment.{Live, TestClock}

object StreamSpec
    extends DefaultRunnableSpec(
      suite("timings")(
        testM("first attempt") {
          val stream = StreamProducer.stream
            .take(100)
          val sink = Sink .collectAll[SimpleEvent]
          for {
            _ <- Live.withLive(TestClock.adjust(10.seconds))(
              _.repeat(ZSchedule.spaced(10.millis))).fork
            runner <- stream
              .run(sink)
          } yield assert(runner.size, equalTo(100))
        }
      )
    )
