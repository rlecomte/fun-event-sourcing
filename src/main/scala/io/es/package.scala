package io

import cats.data.ReaderWriterStateT

package object es {
  type Result[A] = Either[String, A]
  type EventHandler[STATE, EVENT] = (Option[STATE], EVENT) => STATE
  type Sourcing[STATE, EVENT, A] = ReaderWriterStateT[Result, Unit, Vector[EVENT], STATE, A]

  object EventHandler {
    def apply[STATE, EVENT](eventHandler: EventHandler[STATE, EVENT]): EventHandler[STATE, EVENT] = eventHandler
  }
}
