package io.es.domain.turtle

import java.util.UUID

sealed trait TurtleCommand
case object Create extends TurtleCommand
case class Walk(id: UUID, dist: Int) extends TurtleCommand