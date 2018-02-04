package io.es.infra

import cats.Monoid
import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits._
import io.es.infra.Sourced.{CreateSource, Source, UpdateSource}
import io.es.infra.data.{Aggregate, Command, Event, _}
import shapeless.{TypeCase, Typeable}

case class DomainSystem[P](list: List[EventJournal[P] => PartialFunction[Command, IO[CommandResult]]] = Nil) {

  def apply(journal: EventJournal[P]): Command => IO[CommandResult] = {
    val partialFunctions = list.map(_.apply(journal))
    (cmd: Command) => {
      partialFunctions
        .find(_.isDefinedAt(cmd)).map(f => f(cmd))
        .getOrElse(IO.raiseError(new RuntimeException(s"Command $cmd not supported.")))
    }
  }
}

object DomainSystem {

  implicit def monoidDomainSystem[P]: Monoid[DomainSystem[P]] = new Monoid[DomainSystem[P]] {
    override def empty: DomainSystem[P] = DomainSystem()

    override def combine(x: DomainSystem[P], y: DomainSystem[P]): DomainSystem[P] = DomainSystem(x.list ++ y.list)
  }

  def aggregateSystem[A <: Aggregate, E <: Event, C <: Command, P](
    aggregateTag: String,
    commandHandler: C => IO[Source[A, E, Unit]],
    eventHandler: EventHandler[A, E],
    encoder: EventEncoder[E, P],
    decoder: EventDecoder[E, P]
  )(implicit typeable: Typeable[C]): DomainSystem[P] = {

    val compliantCommand = TypeCase[C]

    val partialApply: EventJournal[P] => PartialFunction[Command, IO[CommandResult]] = { journal =>
      PartialFunction[Command, IO[CommandResult]] {
        case compliantCommand(c) =>
          commandHandler(c).flatMap {
            case cs@CreateSource(_, _) =>

              cs.values.value match {
                case Right((events, state, _)) =>
                  for {
                    _ <- journal.write(encoder, decoder, aggregateTag)(state.aggregateId, Version.origin, events)
                  } yield SuccessCommand(state.aggregateId)
                case Left(message) => IO.pure(FailedCommand(message))
              }

            case us@UpdateSource(_) =>

              journal.hydrate(decoder, eventHandler, c.aggregateId).flatMap {
                case Some((state, version)) =>
                  us.sourced.run((), state) match {
                    case Right((events, newState, _)) =>
                      for {
                        _ <- journal.write(encoder, decoder, aggregateTag)(newState.aggregateId, version, NonEmptyList.fromList(events).get)
                      } yield SuccessCommand(newState.aggregateId)
                    case Left(message) => IO.pure[CommandResult](FailedCommand(message))
                  }
                case None => IO.pure(FailedCommand("not.found"))
              }
          }
      }
    }

    DomainSystem(partialApply :: Nil)
  }
}