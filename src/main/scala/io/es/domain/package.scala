package io.es

import io.circe.Json
import io.es.domain.events.json.TurtleEventCirceInstances
import io.es.domain.turtle.Turtle
import io.es.domain.turtle.Turtle.{TurtleCommand, TurtleEvent}
import io.es.infra.{AggregateTag, MultiEventDecoder}
import io.es.store.sql.{JsonEvent, JsonEventDecoder, JsonEventEncoder}
import shapeless.{::, HNil}

package object domain extends TurtleEventCirceInstances {

  implicit val turtleAggregate: AggregateTag.Aux[Turtle, TurtleCommand, TurtleEvent] = AggregateTag("turtle")

  implicit val turtleJsonEventEncoder: JsonEventEncoder[TurtleEvent] = JsonEvent[TurtleEvent].eventEncoder

  implicit val turtleJsonEventDecoder: JsonEventDecoder[TurtleEvent] = JsonEvent[TurtleEvent].eventDecoder

  implicit val multiJsonEventDecoder: MultiEventDecoder[Turtle :: HNil, Json] = MultiEventDecoder.instance
}
