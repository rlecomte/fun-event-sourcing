package io.es.infra

import io.circe.Json
import io.es.UUID

sealed trait Error extends RuntimeException

sealed trait UnexpectedError extends Error
case class HydrateNoAggregateFound(id: UUID) extends UnexpectedError
case class HydrateUncaughtEventError(aggregate: Option[String], event: String) extends UnexpectedError
case class HydrateDecodeEventError(json: Json, err: String) extends UnexpectedError

sealed trait SourceError extends Error
case object SourceUncaughtEventError extends SourceError
case class SourceApplyEventError(error: String) extends SourceError

