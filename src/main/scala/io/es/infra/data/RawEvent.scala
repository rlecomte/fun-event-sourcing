package io.es.infra.data

import io.circe.Json

case class RawEvent(payload: Json) extends AnyVal
