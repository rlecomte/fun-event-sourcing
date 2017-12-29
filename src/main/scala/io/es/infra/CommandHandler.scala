package io.es.infra

import cats.data.Reader
import cats.effect.IO
import io.es.infra.Sourced.{CreateSource, Source, UpdateSource}
import io.es.infra.data._
import cats.implicits._
import io.es.infra.EventDecoder.EventDecoder
import io.es.infra.EventEncoder.EventEncoder

case class CommandHandler[C <: Command, S <: Aggregate, E <: Event](before: Reader[C, IO[Unit]], handler: Reader[C, Source[S, E, Unit]], after: Reader[(C, CommandResult), IO[Unit]]) {

  def runCommand[P, T <: String](cmd: C, journal: EventJournal[P])(
    implicit aggregate: AggregateTag.Aux[S, C, E],
    encoder: EventEncoder[E, P],
    decoder: EventDecoder[E, P],
    eHandler: EventHandler[S, E]
  ): IO[CommandResult] = {
    val mainIO: IO[CommandResult] = handler(cmd) match {
      case cs@CreateSource(_, _) =>

        cs.values.value match {
          case Right((events, state, _)) =>
            for {
              _ <- journal.write(state.aggregateId, 0, events)
            } yield SuccessCommand(state.aggregateId, state, events)
          case Left(message) => IO.pure(FailedCommand(message))
        }

      case us@UpdateSource(_) =>

        cmd.aggregateId match {
          case Some(uuid) => journal.hydrate(uuid).flatMap {
            case Some((state, version)) =>
              us.sourced.run((), state) match {
                case Right((events, newState, _)) =>
                  for {
                    _ <- journal.write(newState.aggregateId, version, events)
                  } yield SuccessCommand(newState.aggregateId, newState, events)
                case Left(message) => IO.pure(FailedCommand(message))
              }
            case None => IO.pure(FailedCommand("not.found"))
          }
          case None => IO.pure(FailedCommand("not.found"))
        }
    }

    for {
      _ <- before(cmd)
      r <- mainIO
      _ <- after((cmd, r))
    } yield r
  }

  def beforeHandler(processBefore: C => IO[Unit]): CommandHandler[C, S, E] = {
    copy(before = Reader(processBefore))
  }

  def afterHandler(processAfter: (C, CommandResult) => IO[Unit]): CommandHandler[C, S, E] = {
    copy(after = Reader(processAfter.tupled))
  }
}

object CommandHandler {
  def handle[C <: Command, S <: Aggregate, E <: Event](
    processBefore: C => IO[Unit] = (_: C) => IO.unit,
    handler: C => Source[S, E, Unit],
    processAfter: (C, CommandResult) => IO[Unit] = (_: C, _: CommandResult) => IO.unit
  ): CommandHandler[C, S, E] = CommandHandler(Reader(processBefore), handler = Reader(handler), Reader(processAfter.tupled))
}
