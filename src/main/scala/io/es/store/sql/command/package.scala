package io.es.store.sql

import io.es.infra.data.Command
import io.es.turtle.Turtle.{CreateCmd, TurtleCmd, WalkCmd, WalkLeftCmd, WalkRightCmd}

package object command {

  implicit val turtleCommand: Command[TurtleCmd] = {
    case CreateCmd(_, _) => None
    case WalkRightCmd(id, _) => Some(id)
    case WalkLeftCmd(id, _) => Some(id)
    case WalkCmd(id, _) => Some(id)
  }

}
