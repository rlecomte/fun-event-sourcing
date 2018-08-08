package io.es.infra.sql

import cats.effect.IO
import cats.~>
import io.es.algebra.JournalOps

object PostgresJournalInterpreter extends (JournalOps ~> IO) {

  override def apply[A](fa: JournalOps[A]): IO[A] = ???
}
