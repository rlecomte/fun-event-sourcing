package io.es.algebra

import io.es.infra.Repository.SourceResult
import io.es.infra.data.{Aggregate, EventFormat}

trait SourceLogger[F[_]] {

  def logResult[S, E](result: SourceResult[S, E])
                           (implicit aggregate: Aggregate[S, E], format: EventFormat[E]): F[Unit]
}

object SourceLogger {
  def apply[F[_]](implicit logger: SourceLogger[F]): SourceLogger[F] = logger
}