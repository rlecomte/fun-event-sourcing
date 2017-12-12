package io.es.infra

import cats.data.Reader
import cats.effect.IO
import io.es.infra.Sourced.{CreateSource, Source, UpdateSource}
import io.es.infra.data._
import cats.implicits._

case class CommandHandler[C, S, E](before: Reader[C, IO[Unit]], handler: Reader[C, Source[S, E, Unit]], after: Reader[(C, CommandResult), IO[Unit]]) {

  def runCommand[P](cmd: C, journal: EventJournal[IO, P])(
    implicit e: Event[E, P],
    aggregate: Aggregate[S],
    command: Command[C],
    eHandler: EventHandler[S, E]
  ): IO[CommandResult] = {
    val mainIO: IO[CommandResult] = handler(cmd) match {
      case cs@CreateSource(_, _) =>

        cs.values.value match {
          case Right((events, state, _)) =>
            for {
              _ <- journal.write(aggregate.aggregateId(state), 0, events)
            } yield CmdOK(aggregate.aggregateId(state), state, events)
          case Left(message) => IO.pure(CmdNOK(message))
        }

      case us@UpdateSource(_) =>

        command.aggregateId(cmd) match {
          case Some(uuid) => journal.hydrate(uuid).flatMap {
            case Some((state, version)) =>
              us.sourced.run((), state) match {
                case Right((events, newState, _)) =>
                  for {
                    _ <- journal.write(aggregate.aggregateId(newState), version, events)
                  } yield CmdOK(aggregate.aggregateId(newState), newState, events)
                case Left(message) => IO.pure(CmdNOK(message))
              }
            case None => IO.pure(CmdNOK("not.found"))
          }
          case None => IO.pure(CmdNOK("not.found"))
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
  def handle[C, S, E](
    processBefore: C => IO[Unit] = (_: C) => IO.unit,
    handler: C => Source[S, E, Unit],
    processAfter: (C, CommandResult) => IO[Unit] = (_: C, _: CommandResult) => IO.unit
  ): CommandHandler[C, S, E] = CommandHandler(Reader(processBefore), handler = Reader(handler), Reader(processAfter.tupled))
}
