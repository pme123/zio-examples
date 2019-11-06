package pme123.zio.examples.timpigden.stream

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import pme123.zio.examples.timpigden.stream.StreamTests.Delay
import zio._
import zio.clock.Clock
import zio.duration._
import zio.stream.{Sink, ZStream}
import zio.test.Assertion._
import zio.test._
import zio.test.environment.{Live, TestClock}

object StreamTests {

  case class Delay(amount: Duration)

  def fastTime(
      testIntervall: Duration,
      liveIntervall: Duration
  ): ZIO[TestClock with Live[Clock], Nothing, Fiber[Nothing, Int]] =
    Live
      .withLive(TestClock.adjust(testIntervall))(
        _.repeat(ZSchedule.spaced(liveIntervall))
      )
      .fork

  def randomEventDelayStream[Evt <: Event](
      inStream: ZStream[ZEnv with Clock, Nothing, Evt]
  ) =
    inStream.mapM { ev =>
      randomDuration(10.millis, 10.seconds).map { d =>
        val receivedTime = ev.at.plus(d.toMillis, ChronoUnit.MILLIS)
        ReceivedEvent(ev, receivedTime)
      }
    }

  def randomDuration(duration: Duration, duration1: Duration) =
    random
      .nextLong(duration1.toMillis)
      .map(d => Duration(d - duration.toMillis, TimeUnit.MILLISECONDS))

  /**
    * arbitrarily generate delays or reduce arrays already there
    * @param howOften how often do we actually get delays
    * @param variation max delay we want to create
    * @param standardDelay all messages are delayed by a few milliseconds
    * @param sampleFrequency how frequently our source main stream set is generating events
    */
  def delayGenerator(
      howOften: Duration,
      variation: Duration,
      standardDelay: Duration,
      sampleFrequency: Duration
  ): EventGenerator[Delay, Delay] =
    new EventGenerator[Delay, Delay] {
      private val sampleFrequencyMillis = sampleFrequency.toMillis
      private val howOftenDbl = sampleFrequency.toMillis.toDouble / howOften.toMillis // we will use this to get probability of introducing new delay
      private val standardDelayMillis = standardDelay.toMillis

      override def generate(
          s: Delay
      ): ZIO[zio.ZEnv, Nothing, Option[(Delay, Delay)]] = {
        val sMillis = s.amount.toMillis
        val newMillis =
          if (sMillis > sampleFrequency.toMillis)
            IO.succeed(
              Duration(
                sMillis - sampleFrequency.toMillis,
                TimeUnit.MILLISECONDS
              )
            )
          else if (sMillis > standardDelayMillis)
            IO.succeed(standardDelay)
          else
            for {
              r <- random.nextDouble
              newDelay <- if (r > howOftenDbl) // we don't want a new one
                IO.succeed(standardDelay)
              else randomDuration(standardDelay, variation)
            } yield newDelay
        newMillis.map { nd =>
          Some((Delay(nd), Delay(nd)))
        }
      }
    }
}

object StreamSpec
    extends DefaultRunnableSpec(
      suite("Streaming")(
        suite("timings")(
          testM("first attempt") {
            val stream = StreamProducer.stream
              .take(100)
            val sink = Sink.collectAll[SimpleEvent]
            for {
              _ <- StreamTests.fastTime(10.seconds, 10.milliseconds)
              runner <- stream
                .run(sink)
            } yield assert(runner.size, equalTo(100))
          }
        ),
        suite("test emitting stream")(
          testM("random walk") {
            for {
              nw <- clock.currentDateTime
              initialState = ChillEvent("vehicle1", -18.0, nw.toInstant)
              _ <- StreamTests.fastTime(10.seconds, 10.milliseconds)
              randomWalker = EventGenerator
                .centeringRandomWalkGenerator(1, -10.0, 0.1)
              stream = EventGenerator
                .generatedStream(initialState, randomWalker, 1.minutes)
                .take(200)
              sink = Sink.collectAll[ChillEvent]
              runner <- stream.run(sink)
              _ <- Live.live(console.putStrLn(s"${runner.mkString("\n")}"))
            } yield {
              assert(runner.size, equalTo(200))
            }
          },
          testM("run delay") {
            val initialState = Delay(0.second)
            val delayer = StreamTests.delayGenerator(
              howOften = 10.minutes,
              variation = 5.minutes,
              standardDelay = 10.millis,
              sampleFrequency = 20.seconds
            )
            val delays =
              ZStream.unfoldM(initialState)(delayer.generate).take(200)
            val sink = Sink.collectAll[Delay]
            println("delays")
            for {
              runner <- delays.run(sink)
              _ <- Live.live(console.putStrLn(s"${runner.mkString("\n")}"))
            } yield {
              assert(runner.size, equalTo(200))
            }
          },
          testM("run delayed chills") {
            val initialDelay = Delay(0.second)
            val delayer = StreamTests.delayGenerator(
              howOften = 10.minutes,
              variation = 5.minutes,
              standardDelay = 10.millis,
              sampleFrequency = 20.seconds
            )
            val delays = ZStream.unfoldM(initialDelay)(delayer.generate)
            for {
              _ <- StreamTests.fastTime(10.seconds, 5.millis)
              nw <- clock.currentDateTime
              initialChill = ChillEvent("vehicle1", -18.0, nw.toInstant)
              randomWalker = EventGenerator
                .centeringRandomWalkGenerator(1, -10.0, 0.1)
              chillStream = EventGenerator.generatedStream(
                initialChill,
                randomWalker,
                20.seconds
              )
              receivedEvents = delays
                .zip(chillStream)
                .map { pair =>
                  ReceivedEvent(pair._2, pair._2.at.plus(pair._1.amount.toMillis, ChronoUnit.MILLIS))
                }
                .take(200)
              sink = Sink.collectAll[ReceivedEvent[ChillEvent]]
              runner <- receivedEvents.run(sink)
              _ <- Live.live(console.putStrLn(s"${runner.mkString("\n")}"))
            } yield {
              assert(runner.size, equalTo(200))
            }
          },testM("multiple vehicles"){
            val initialDelay = Delay(0.second)
            val delayer = StreamTests.delayGenerator(
              howOften = 10.minutes,
              variation = 5.minutes,
              standardDelay = 10.millis,
              sampleFrequency = 20.seconds
            )
            val randomWalker = EventGenerator.centeringRandomWalkGenerator(1, -10.0, 0.1)

            def vehicleStream(i: Int, startAt: Instant) = {
              val initialChill = ChillEvent(s"v-$i", -18.0, startAt)
              val chillStream = EventGenerator.generatedStream(initialChill, randomWalker, 20.seconds)
              val delays = ZStream.unfoldM(initialDelay)(delayer.generate)
              delays.zip(chillStream).map { pair =>
                ReceivedEvent(pair._2, pair._2.at.plus(pair._1.amount.toMillis, ChronoUnit.MILLIS))
              }
            }

            for {
              _ <- StreamTests.fastTime(10.seconds, 5.millis)
              nw <- clock.currentDateTime
              streams = 1.to(20).map { v => vehicleStream(v, nw.toInstant)}
              combined = ZStream.mergeAllUnbounded()(streams:_*)
              sink = Sink.collectAll[ReceivedEvent[ChillEvent]]
              runner <- combined.take(2000).run(sink)
              _ <- Live.live(console.putStrLn(s"${runner.mkString("\n")}"))
            } yield {
              assert(runner.size, equalTo(2000))
            }
          }

        )
      )
    )
