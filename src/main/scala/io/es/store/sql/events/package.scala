package io.es.store.sql

import io.es.store.sql.JsonEvent.JsonEvent
import io.es.turtle.Turtle.TurtleEvent

package object events extends TurtleEventCirceInstances {

  implicit val turtleJsonEvent: JsonEvent[TurtleEvent] = JsonEvent.fromCirceInstances("turtle")
}
