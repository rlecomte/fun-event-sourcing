package io.es.infra.data

import io.es.UUID

trait Command[C] {
  def aggregateId(c: C): Option[UUID]
}