package io.es

import io.circe.Json
import io.es.domain.events.json.TurtleEventCirceInstances
import io.es.domain.turtle.Turtle
import io.es.domain.turtle.Turtle.TurtleEvent
import io.es.infra.DomainSystem
import io.es.store.sql.JsonEvent

package object domain extends TurtleEventCirceInstances {

  val turtleSystem: DomainSystem[Json] = DomainSystem.aggregateSystem(
    aggregateTag = "turtle",
    commandHandler = Turtle.turtleCommandHandler,
    eventHandler = Turtle.turtleEventHandler,
    encoder = JsonEvent.encoder[TurtleEvent],
    decoder = JsonEvent.decoder[TurtleEvent]
  )

  //val fooSystem: DomainSystem[Json] = ???

  //import cats.implicits._
  val mainSystem: DomainSystem[Json] = turtleSystem// |+| fooSystem
}
