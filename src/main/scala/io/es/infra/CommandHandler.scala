package io.es.infra

import cats.data.Reader
import cats.effect.IO
import io.es.infra.Sourced.{CreateSource, Source, UpdateSource}
import io.es.infra.data._
import cats.implicits._

case class CommandHandler[C, S, E](before: Reader[C, IO[Unit]], handler: Reader[C, Source[S, E, Unit]], after: Reader[(C, Result[(S, E)]), IO[Unit]]) {

  def runCommand[P](cmd: C, journal: EventJournal[IO, P])(
    implicit e: Event[E, P],
    aggregate: Aggregate[S],
    command: Command[C],
    eHandler: EventHandler[S, E]
  ): IO[CommandResult] = {
    handler(cmd) match {
      case cs@CreateSource(_, _) =>

        cs.values.value match {
          case Right((events, state, _)) =>
            for {
              _ <- journal.write(aggregate.aggregateId(state), 0, events)
            } yield CmdOK(aggregate.aggregateId(state))
          case Left(message) => IO.pure(CmdNOK(message))
        }

      case us@UpdateSource(_) =>

        command.aggregateId(cmd) match {
          case Some(uuid) => journal.hydrate(uuid).flatMap {
            case Some(state) =>
              us.sourced.run((), state) match {
                case Right((events, newState, _)) =>
                  for {
                    _ <- journal.write(aggregate.aggregateId(newState), 0, events)
                  } yield CmdOK(aggregate.aggregateId(newState))
                case Left(message) => IO.pure(CmdNOK(message))
              }
            case None => IO.pure(CmdNOK("not.found"))
          }
          case None => IO.pure(CmdNOK("not.found"))
        }
    }
  }

  def beforeHandler(processBefore: C => IO[Unit]): CommandHandler[C, S, E] = {
    copy(before = Reader(processBefore))
  }

  def afterHandler(processAfter: (C, Result[(S, E)]) => IO[Unit]): CommandHandler[C, S, E] = {
    copy(after = Reader(processAfter.tupled))
  }
}

object CommandHandler {
  def handle[C, S, E](
    processBefore: C => IO[Unit] = (_: C) => IO.unit,
    handler: C => Source[S, E, Unit],
    processAfter: (C, Result[(S, E)]) => IO[Unit] = (_: C, _: Result[(S, E)]) => IO.unit
  ): CommandHandler[C, S, E] = CommandHandler(Reader(processBefore), handler = Reader(handler), Reader(processAfter.tupled))
}
