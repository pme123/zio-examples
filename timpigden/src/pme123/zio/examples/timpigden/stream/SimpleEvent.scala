package pme123.zio.examples.timpigden.stream

import java.time.Instant

trait Event {
  def at: Instant
}

case class SimpleEvent(at: Instant) extends Event

case class ChillEvent(vehicleId: String, temperature: Temperatur, at: Instant) extends Event

case class ReceivedEvent[Evt](event: Evt, receivedAt: Instant) extends Event {
  override def at: Instant = receivedAt
}
