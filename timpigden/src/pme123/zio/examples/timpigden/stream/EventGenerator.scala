package pme123.zio.examples.timpigden.stream

import zio._
import zio.duration.Duration
import zio.stream.ZStream
import zio.random._
import zio.clock

trait EventGenerator[Evt, S] {
  def generate(s: S): ZIO[ZEnv, Nothing, Option[(Evt, S)]]
}

object EventGenerator {


  def generatedStream[Evt, S](initialState: S, generator: EventGenerator[Evt, S], timing: Duration) =
    ZStream.unfoldM(initialState)(generator.generate)
      .schedule(ZSchedule.spaced(timing))

  /**
   * creates an EventGenerator implementing biased random walk. Note this is not
   * intended to emulate real refrigeration units - just give us some numbers to play with
   * @param variation maximum we should change at each tick
   * @param centre target temperature to which we are biased
   * @param bias number in range 0 - 1 representing the proportion
   *             of ticks at which we attempt to move towards the centre
   */
  def centeringRandomWalkGenerator(variation: Double, centre: Temperatur, bias: Double): EventGenerator[ChillEvent, ChillEvent] = { s =>
    for {
      random <- randomService
      d1a <- random.nextDouble
      d1 = d1a * 2 - 1
      d2 <- random.nextDouble
      rawAmount = if (d2 < bias) {
        // we want to move towards centre
        val direction = if (s.temperature > centre) -1 else 1
        Math.abs(d1) * direction
      } else d1
      adjustedAmount = rawAmount * variation
      nw <- clock.currentDateTime // gets the current time from, the clock
      newEvent = s.copy(temperature = adjustedAmount, at = nw.toInstant)
    } yield Some((newEvent, newEvent))
  }
}

