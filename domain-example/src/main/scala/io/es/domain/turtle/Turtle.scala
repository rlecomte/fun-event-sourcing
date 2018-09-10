package io.es.domain.turtle

import java.util.UUID

import io.es.infra.data.{Aggregate, Tag}

case class Turtle(id: UUID, pos: Position, dir: Direction)

object Turtle extends TurtleAggregateInstance {

  private def withinRange(pos: Position): Boolean =
    pos.x.abs < 100 && pos.y.abs < 100

  def create(id: UUID, pos: Position, dir: Direction): Either[String, TurtleEvent] =
    if (withinRange(pos)) Right(TurtleCreated(id, pos, dir))
    else Left("Too far away")

  def turn(rot: Rotation)(turtle: Turtle): Either[String, TurtleEvent] =
    Right(TurtleDirectionChanged(turtle.id, rot))

  def walk(dist: Int)(turtle: Turtle): Either[String, TurtleEvent] = {
    val newPos = turtle.pos.move(turtle.dir, dist)
    if (withinRange(newPos)) Right(TurtleMoved(turtle.id, dist))
    else Left("Too far away")
  }
}

trait TurtleAggregateInstance {

  val turtleAggregate: Aggregate[Turtle, TurtleEvent] =
    new Aggregate[Turtle, TurtleEvent] {
      override def tag: Tag = Tag("turtle")

      override def id(aggregate: Turtle): UUID = aggregate.id

      override def handle(aggregate: Option[Turtle])(event: TurtleEvent): Option[Turtle] =
        (aggregate, event) match {
          case (None, TurtleCreated(id, pos, dir)) => Some(Turtle(id, pos, dir))
          case (Some(t), TurtleDirectionChanged(id, rot)) if id == t.id =>
            Some(t.copy(dir = t.dir.rotate(rot)))
          case (Some(t), TurtleMoved(id, dist)) if id == t.id =>
            Some(t.copy(pos = t.pos.move(t.dir, dist)))
          case _ => None
        }
    }
}
