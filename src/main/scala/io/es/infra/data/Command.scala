package io.es.infra.data

import io.es.UUID

trait Command {
  def aggregateId: Option[UUID]
}