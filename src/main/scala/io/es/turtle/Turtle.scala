package io.es.turtle

import io.es.infra.EventHandler
import io.es.infra.Sourced.{UpdateSource, source}

case class Turtle(id: String, pos: Position, dir: Direction)

object Turtle {

  sealed trait TurtleCmd
  case class CreateCmd(pos: Position, dir: Direction) extends TurtleCmd
  case class WalkRightCmd(dist: Int) extends TurtleCmd
  case class WalkLeftCmd(dist: Int) extends TurtleCmd
  case class WalkCmd(dist: Int) extends TurtleCmd

  sealed trait TurtleEvent
  case class Create(id: String, pos: Position, dir: Direction) extends TurtleEvent
  case class Turn(id: String, rot: Rotation) extends TurtleEvent
  case class Walk(id: String, dist: Int) extends TurtleEvent

  private def withinRange(pos: Position): Boolean = pos.x.abs < 100 && pos.y.abs < 100

  def create(id: String, pos: Position, dir: Direction): Either[String, TurtleEvent] =
    if (withinRange(pos)) Right(Create(id, pos, dir))
    else Left("Too far away")

  def turn(rot: Rotation)(turtle: Turtle): Either[String, TurtleEvent] =
    Right(Turn(turtle.id, rot))

  def walk(dist: Int)(turtle: Turtle): Either[String, TurtleEvent] = {
    val newPos = turtle.pos.move(turtle.dir, dist)
    if (withinRange(newPos)) Right(Walk(turtle.id, dist))
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
    case (Some(t), Turn(id, rot)) if id == t.id =>
      t.copy(dir = t.dir.rotate(rot))
    case (Some(t), Walk(id, dist)) if id == t.id =>
      t.copy(pos = t.pos.move(t.dir, dist))
  }
}
