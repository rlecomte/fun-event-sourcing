package io.es.store.sql

import io.es.infra.data.Aggregate
import io.es.turtle.Turtle

package object aggregate {

  implicit val turtleAggregate: Aggregate[Turtle] = (aggregate: Turtle) => aggregate.id
}
