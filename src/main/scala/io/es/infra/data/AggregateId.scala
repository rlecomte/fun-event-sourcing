package io.es.infra.data

import io.es.UUID

case class AggregateId(value: UUID) extends AnyVal

object AggregateId {

  def newId(): AggregateId = AggregateId(java.util.UUID.randomUUID())

  val zero: AggregateId = AggregateId(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"))
}