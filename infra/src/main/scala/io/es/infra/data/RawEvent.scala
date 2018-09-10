package io.es.infra.data

import io.circe.Json

/**
  * Represent the raw value of an event
  * @param payload
  */
case class RawEvent(payload: Json) extends AnyVal
