package io.es.infra

import cats.effect.Sync
import cats.implicits._
import Source.{HydratedSource, NewSource}
import io.es._
import io.es.infra.data.{Aggregate, EventFormat}

abstract class Repository[F[_]: Sync, S, E] {

  def get(id: java.util.UUID): F[S]

  def save(source: Source[S, E]): F[SourceResult[S, E]]

  def applySource(source: Source[S, E])(implicit aggregate: Aggregate[S, E]): F[SourceResult[S, E]] =  {
    def applyPartialSource(state: S, events: Option[E])(list: List[S => Result[E]]): F[SourceResult[S, E]] = Sync[F].suspend {
      var currentState = state
      var currentEvents = events.map(e => e :: Nil).getOrElse(Nil)
      var fun = list.reverse

      while(fun.nonEmpty) {
        fun.head(currentState) match {
          case Right(event) =>
            currentEvents = event :: currentEvents
            currentState = aggregate.handle(Some(currentState))(event)
          case Left(err) =>
            return Sync[F].raiseError(new RuntimeException("fail : " + err))
        }
        fun = fun.tail
      }

      Sync[F].pure(SourceResult(currentState, currentEvents.reverse))
    }

    source match {
      case Source(NewSource(f), PartialSource(list)) =>
        f match {
          case Right(event) =>
            applyPartialSource(aggregate.handle(None)(event), Some(event))(list)
          case Left(err) => Sync[F].raiseError(new RuntimeException("fail : " + err))
        }

      case Source(HydratedSource(id), PartialSource(list)) =>
        get(id).flatMap(s => applyPartialSource(s, None)(list))
    }
  }
}

class StandardRepository[F[_]: Sync, S, E](journal: Journal[F])(implicit aggregate: Aggregate[S, E], format: EventFormat[E]) extends Repository[F, S, E] {

  def get(id: java.util.UUID): F[S] = {
    journal.hydrate(id)
      .evalMap(format.decode)
      .fold(Option.empty[S])((s, e) => Some(aggregate.handle(s)(e)))
      .compile.last.map(_.flatten)
      .flatMap {
        case Some(s) =>
          Sync[F].pure(s)
        case None =>
          Sync[F].raiseError(throw new RuntimeException(s"no aggregate for id $id"))
      }
  }

  def save(source: Source[S, E]): F[SourceResult[S, E]] = {
    applySource(source).flatMap { r =>
      journal
        .register(aggregate.tag, aggregate.id(r.state), r.events.map(format.encode))
        .map(_ => r)
    }
  }
}

class InMemoryRepository[F[_]: Sync, S, E] extends Repository[F, S, E] {
  override def get(id: UUID): F[S] = ???

  override def save(source: Source[S, E]): F[SourceResult[S, E]] = ???
}