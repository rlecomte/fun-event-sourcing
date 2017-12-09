package io.es.turtle

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