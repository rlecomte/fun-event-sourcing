package io.es.infra.data

import io.es.UUID

sealed trait CommandResult
case class CmdNOK(message: String) extends CommandResult
case class CmdOK[S, E](id: UUID, state: S, event: List[E]) extends CommandResult
