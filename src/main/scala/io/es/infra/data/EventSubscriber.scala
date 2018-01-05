package io.es.infra.data

import cats.effect.IO

case class EventSubscriber(id: EventSubscriberId, handler: PartialFunction[Event, IO[Unit]])
