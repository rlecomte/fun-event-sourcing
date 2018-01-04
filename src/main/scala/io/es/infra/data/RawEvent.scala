package io.es.infra.data

import java.time.ZonedDateTime

import io.es.UUID

case class RawEvent[P](
  aggregateId: UUID,
  version: Version,
  seqNumber: SequenceNumber,
  data: P,
  timestamp: ZonedDateTime,
  aggregateType: String
)
