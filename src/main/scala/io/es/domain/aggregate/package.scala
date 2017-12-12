package io.es.domain

import io.es.infra.data.Aggregate
import io.es.domain.turtle.Turtle

package object aggregate {

  implicit val turtleAggregate: Aggregate[Turtle] = (aggregate: Turtle) => aggregate.id
}
