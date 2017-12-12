package io.es

import cats.effect.IO
import io.es.infra.CommandHandler
import io.es.turtle.Turtle

object TurtleCommandHandler {

  import Turtle._
  import io.es.infra.Sourced._

  val turtleHandler: CommandHandler[TurtleCmd, Turtle, TurtleEvent] = CommandHandler.handle(
    cmd => IO(println(s"Handle turtle command $cmd")), {
      case CreateCmd(pos, dir) =>
        sourceNew(create(java.util.UUID.randomUUID().toString, pos, dir))
      case WalkRightCmd(dist) =>
        walkRight(dist)
      case WalkLeftCmd(dist) =>
        walkLeft(dist)
      case WalkCmd(dist) =>
        source(walk(dist))
    },
    (_, result) => IO(println(s"Handler result : $result"))
  )
}