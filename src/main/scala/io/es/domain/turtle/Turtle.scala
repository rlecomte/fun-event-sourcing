package io.es.domain.turtle

import io.es.infra.EventHandler
import io.es.infra.Sourced.{UpdateSource, source}
import io.es.infra.data.{Aggregate, AggregateId, Command, Event}

case class Turtle(aggregateId: AggregateId, pos: Position, dir: Direction) extends Aggregate

object Turtle {

  sealed trait TurtleCommand extends Command
  case class CreateCmd(pos: Position, dir: Direction) extends TurtleCommand {
    override def aggregateId: Option[AggregateId] = None
  }

  case class WalkRightCmd(id: AggregateId, dist: Int) extends TurtleCommand {
    override def aggregateId: Option[AggregateId] = Some(id)
  }

  case class WalkLeftCmd(id: AggregateId, dist: Int) extends TurtleCommand {
    override def aggregateId: Option[AggregateId] = Some(id)
  }

  case class WalkCmd(id: AggregateId, dist: Int) extends TurtleCommand {
    override def aggregateId: Option[AggregateId] = Some(id)
  }

  sealed trait TurtleEvent extends Event
  case class Create(id: AggregateId, pos: Position, dir: Direction) extends TurtleEvent
  case class Turn(id: AggregateId, rot: Rotation) extends TurtleEvent
  case class Walk(id: AggregateId, dist: Int) extends TurtleEvent

  private def withinRange(pos: Position): Boolean = pos.x.abs < 100 && pos.y.abs < 100

  def create(id: AggregateId, pos: Position, dir: Direction): Either[String, TurtleEvent] =
    if (withinRange(pos)) Right(Create(id, pos, dir))
    else Left("Too far away")

  def turn(rot: Rotation)(turtle: Turtle): Either[String, TurtleEvent] =
    Right(Turn(turtle.aggregateId, rot))

  def walk(dist: Int)(turtle: Turtle): Either[String, TurtleEvent] = {
    val newPos = turtle.pos.move(turtle.dir, dist)
    if (withinRange(newPos)) Right(Walk(turtle.aggregateId, dist))
    else Left("Too far away")
  }

  def walkRight(dist: Int): UpdateSource[Turtle, TurtleEvent, Unit] = {
    for {
      _ <- source(Turtle.turn(ToRight))
      _ <- source(Turtle.walk(dist))
    } yield ()
  }

  def walkLeft(dist: Int): UpdateSource[Turtle, TurtleEvent, Unit] = {
    for {
      _ <- source(Turtle.turn(ToLeft))
      _ <- source(Turtle.walk(dist))
    } yield ()
  }

  implicit val turtleEventHandler: EventHandler[Turtle, TurtleEvent] = EventHandler[Turtle, TurtleEvent] {
    case (None, Create(id, pos, dir)) => Turtle(id, pos, dir)
    case (Some(t), Turn(id, rot)) if id == t.aggregateId =>
      t.copy(dir = t.dir.rotate(rot))
    case (Some(t), Walk(id, dist)) if id == t.aggregateId =>
      t.copy(pos = t.pos.move(t.dir, dist))
    case _ => throw new RuntimeException("Unexpected state.")
  }
}
