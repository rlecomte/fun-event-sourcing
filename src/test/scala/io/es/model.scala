package io.es

import io.es.data.Aggregate

object model {

  sealed trait Rotation
  case object ToLeft extends Rotation; case object ToRight extends Rotation

  sealed trait Direction {
    def rotate(rot: Rotation): Direction =
      (this, rot) match {
        case (North, ToLeft) => West; case (North, ToRight) => Est
        case (South, ToLeft) => Est; case (South, ToRight) => West
        case (Est, ToLeft) => North; case (Est, ToRight) => South
        case (West, ToLeft) => South; case (West, ToRight) => North
      }
  }
  case object North extends Direction; case object South extends Direction
  case object Est extends Direction; case object West extends Direction

  case class Position(x: Int, y: Int) {
    def move(dir: Direction, distance: Int): Position =
      dir match {
        case North => copy(y = y + distance); case South => copy(y = y - distance)
        case Est => copy(x = x + distance); case West => copy(x = x - distance)
      }
  }
  object Position {
    val zero = Position(0, 0)
    def move(pos: Position, dir: Direction, distance: Int): Position =
      dir match {
        case North => pos.copy(y = pos.y + distance); case South => pos.copy(y = pos.y - distance)
        case Est => pos.copy(x = pos.x + distance); case West => pos.copy(x = pos.x - distance)
      }
  }

  case class Turtle(id: String, pos: Position, dir: Direction) extends Aggregate

  object Turtle {

    sealed trait TurtleEvent { def id: String }
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

    implicit val handler = EventHandler[Turtle, TurtleEvent] {
      case (None, Create(id, pos, dir)) => Turtle(id, pos, dir)
      case (Some(t), Turn(id, rot)) if id == t.id =>
        t.copy(dir = t.dir.rotate(rot))
      case (Some(t), Walk(id, dist)) if id == t.id =>
        t.copy(pos = t.pos.move(t.dir, dist))
    }
  }
}
