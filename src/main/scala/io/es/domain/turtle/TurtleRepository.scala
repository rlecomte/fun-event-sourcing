package io.es.domain.turtle

import io.es.infra.Repository

object TurtleRepository extends Repository(
  Turtle.turtleAggregate,
  null//TODO
)
