package io.es.infra.data

import io.es.UUID

trait Aggregate[A] {

  def aggregateId(aggregate: A): UUID
}
