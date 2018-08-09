package io.es.infra

import Source.{HydratedSource, NewSource}
import cats.MonadError
import io.es._
import io.es.algebra._
import io.es.infra.data.{Aggregate, EventFormat}

//TODO create specific error type
class Repository[S, E](aggregate: Aggregate[S, E], format: EventFormat[E]) {

  def get(id: java.util.UUID): Stack[S] = journal.hydrate(
    id,
    json => format.decode(json).flatMap(e =>
      aggregate.handle(None)(e)
        .toRight("Uncaught event type.")
    )
  )

  def save(source: Source[S, E])(implicit monadErr: MonadError[Stack, Throwable]): Stack[SourceResult[S, E]] =
    applySource(source).flatMap { r =>
      journal
        .register(aggregate.tag, aggregate.id(r.state), r.events.map(format.encode))
        .map(_ => r)
    }

  private def applySource(source: Source[S, E])(implicit monadErr: MonadError[Stack, Throwable]): Stack[SourceResult[S, E]] =  {

    def applyPartialSource(state: S, firstEvent: Option[E])(list: List[S => Result[E]]): Stack[SourceResult[S, E]] = {
      var rest = list.reverse
      var currentState = state
      var appliedEvents = firstEvent.map(e => e :: Nil).getOrElse(Nil)

      while (rest.nonEmpty) {
        rest.head(currentState)  match {
          case Right(newEvent) =>
            aggregate.handle(Some(currentState))(newEvent) match {
              case Some(newState) =>
                currentState = newState
                appliedEvents = newEvent :: appliedEvents

              case None => return monadErr.raiseError(new RuntimeException("Uncaught event type."))
            }
          case Left(err) => return monadErr.raiseError(new RuntimeException(err))
        }

        rest = rest.tail
      }

      monadErr.pure(SourceResult(currentState, appliedEvents))
    }

    source match {
      case Source(NewSource(f), PartialSource(list)) =>
        f.flatMap(e => aggregate.handle(None)(e).map(s => (s, e)).toRight("Uncaught event type.")) match {
          case Right((state, event)) =>
            applyPartialSource(state, Some(event))(list)
          case Left(err) =>
            monadErr.raiseError(new RuntimeException(err))
        }

      case Source(HydratedSource(id), PartialSource(list)) =>
        get(id).flatMap(s => applyPartialSource(s, None)(list))
    }
  }
}