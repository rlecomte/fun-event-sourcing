package io.es.infra

import cats.effect.IO
import io.es.infra.data.{EventSubscriber, RawEvent}

trait EventBus[P] {
  def publish(events: List[RawEvent[P]]): IO[Unit]

  def subscribe(subscriber: EventSubscriber)
}
