package io.es.infra.data

sealed trait CommandResult
case class FailedCommand(message: String) extends CommandResult
case class SuccessCommand[S, E](id: AggregateId) extends CommandResult
