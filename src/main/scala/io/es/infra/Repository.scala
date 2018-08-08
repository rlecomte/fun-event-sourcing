package io.es.infra

import Source.{HydratedSource, NewSource}
import cats.MonadError
import io.es._
import io.es.algebra._
import io.es.infra.data.{Aggregate, EventFormat}

import scala.annotation.tailrec

class Repository[S, E](implicit aggregate: Aggregate[S, E], format: EventFormat[E]) {

  def get(id: java.util.UUID): Stack[S] = journal.hydrate(
    id,
    json => format.decode(json).map(aggregate.handle(None))
  )

  def save(source: Source[S, E])(implicit monadErr: MonadError[Stack, Throwable]): Stack[SourceResult[S, E]] =
    applySource(source).flatMap { r =>
      journal
        .register(aggregate.tag, aggregate.id(r.state), r.events.map(format.encode))
        .map(_ => r)
    }

  private def applySource(source: Source[S, E])(implicit monadErr: MonadError[Stack, Throwable]): Stack[SourceResult[S, E]] =  {

    def applyPartialSource(state: S, firstEvent: Option[E])(list: List[S => Result[E]]): Stack[SourceResult[S, E]] = {
      @tailrec
      def rec(state: S, events: List[E], funs: List[S => Result[E]]): Stack[SourceResult[S, E]] = funs match {
        case f :: others =>
          f(state)  match {
            case Right(event) =>
              val currentEvents = event :: events
              val currentState = aggregate.handle(Some(state))(event)
              rec(currentState, currentEvents, others)
            case Left(err) =>
              monadErr.raiseError(new RuntimeException(err))
          }
        case Nil => monadErr.pure(SourceResult(state, events))
      }

      rec(state, firstEvent.map(e => e :: Nil).getOrElse(Nil), list.reverse)
    }

    source match {
      case Source(NewSource(f), PartialSource(list)) =>
        f match {
          case Right(event) =>
            applyPartialSource(aggregate.handle(None)(event), Some(event))(list)
          case Left(err) =>
            monadErr.raiseError(new RuntimeException("fail : " + err))
        }

      case Source(HydratedSource(id), PartialSource(list)) =>
        get(id).flatMap(s => applyPartialSource(s, None)(list))
    }
  }
}

object Repository {
  def apply[S, E](
                   implicit aggregate: Aggregate[S, E], format: EventFormat[E]
                 ): Repository[S, E] = new Repository
}