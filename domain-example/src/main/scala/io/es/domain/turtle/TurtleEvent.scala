package io.es.domain.turtle

import io.es.UUID

sealed trait TurtleEvent
case class TurtleCreated(id: UUID, pos: Position, dir: Direction) extends TurtleEvent
case class TurtleDirectionChanged(id: UUID, rot: Rotation)        extends TurtleEvent
case class TurtleMoved(id: UUID, dist: Int)                       extends TurtleEvent
