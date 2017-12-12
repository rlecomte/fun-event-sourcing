package io.es.domain.turtle

import cats.effect.IO
import io.es.infra.CommandHandler

object TurtleCommandHandler {

  import Turtle._
  import io.es.infra.Sourced._

  val turtleHandler: CommandHandler[TurtleCmd, Turtle, TurtleEvent] = CommandHandler.handle(
    cmd => IO(println(s"Handle turtle command $cmd")), {
      case CreateCmd(pos, dir) =>
        sourceNew(create(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"), pos, dir))
      case WalkRightCmd(_, dist) =>
        walkRight(dist)
      case WalkLeftCmd(_, dist) =>
        walkLeft(dist)
      case WalkCmd(_, dist) =>
        source(walk(dist))
    },
    (_, result) => IO(println(s"Handler result : $result"))
  )
}