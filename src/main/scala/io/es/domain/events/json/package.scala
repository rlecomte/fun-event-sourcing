package io.es.domain.events

import io.es.domain.turtle.Turtle.TurtleEvent
import io.es.store.sql.JsonEvent
import io.es.store.sql.JsonEvent.JsonEvent

package object json extends TurtleEventCirceInstances {

  implicit val turtleJsonEvent: JsonEvent[TurtleEvent] = JsonEvent.fromCirceInstances("turtle")
}
