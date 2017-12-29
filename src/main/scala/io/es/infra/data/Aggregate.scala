package io.es.infra.data

import io.es.UUID

trait Aggregate {
  def aggregateId: UUID
}
