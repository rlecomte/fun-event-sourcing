package io.es.infra.data

import io.es.UUID

case class RawEvent[P](aggregateId: UUID, version: Long, seqNumber: Long, data: P, timestamp: Long, aggregateType: String)
