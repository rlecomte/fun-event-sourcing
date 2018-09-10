package io.es.domain.turtle

import io.es.infra.EventStoreAdapter

object TurtleEventStoreA
    extends EventStoreAdapter(
      Turtle.turtleAggregate,
      null //TODO
    )
