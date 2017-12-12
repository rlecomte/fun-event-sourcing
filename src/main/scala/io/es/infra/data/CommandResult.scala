package io.es.infra.data

import io.es.UUID

sealed trait CommandResult
case class FailedCommand(message: String) extends CommandResult
case class SuccessCommand[S, E](id: UUID, state: S, events: List[E]) extends CommandResult
