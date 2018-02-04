package io.es.domain.turtle

import cats.effect.IO
import io.es.infra.EventHandler
import io.es.infra.Sourced.{Source, UpdateSource, source, sourceNew}
import io.es.infra.data.{Aggregate, AggregateId, Command, Event}

case class Turtle(aggregateId: AggregateId, pos: Position, dir: Direction) extends Aggregate

object Turtle {

  sealed trait TurtleCommand extends Command
  case class CreateCmd(aggregateId: AggregateId = AggregateId.newId(), pos: Position, dir: Direction) extends TurtleCommand

  case class WalkRightCmd(aggregateId: AggregateId, dist: Int) extends TurtleCommand

  case class WalkLeftCmd(aggregateId: AggregateId, dist: Int) extends TurtleCommand

  case class WalkCmd(aggregateId: AggregateId, dist: Int) extends TurtleCommand

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

  val turtleCommandHandler: TurtleCommand => IO[Source[Turtle, TurtleEvent, Unit]] = {
    case CreateCmd(id, pos, dir) =>
      IO(sourceNew(create(id, pos, dir)))
    case WalkRightCmd(_, dist) =>
      IO(walkRight(dist))
    case WalkLeftCmd(_, dist) =>
      IO(walkLeft(dist))
    case WalkCmd(_, dist) =>
      IO(source(walk(dist)))
  }
}
