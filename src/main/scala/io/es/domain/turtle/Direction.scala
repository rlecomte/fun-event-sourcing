package io.es.domain.turtle

sealed trait Direction {
  def rotate(rot: Rotation): Direction =
    (this, rot) match {
      case (North, ToLeft) => West; case (North, ToRight) => Est
      case (South, ToLeft) => Est; case (South, ToRight) => West
      case (Est, ToLeft) => North; case (Est, ToRight) => South
      case (West, ToLeft) => South; case (West, ToRight) => North
    }
}

case object North extends Direction

case object South extends Direction

case object Est extends Direction

case object West extends Direction
