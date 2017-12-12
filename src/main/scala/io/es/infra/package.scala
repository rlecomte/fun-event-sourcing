package io.es

import cats.data.ReaderWriterStateT

package object infra {
  type Result[A] = Either[String, A]
  type Sourcing[STATE, EVENT, A] = ReaderWriterStateT[Result, Unit, List[EVENT], STATE, A]

  type EventHandler[STATE, EVENT] = (Option[STATE], EVENT) => STATE

  object EventHandler {
    def apply[STATE, EVENT](eventHandler: EventHandler[STATE, EVENT]): EventHandler[STATE, EVENT] = eventHandler
  }
}