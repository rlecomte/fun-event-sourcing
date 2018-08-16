package io.es.infra

import Source.{HydratedSource, NewSource}
import cats.{Functor, Monad, MonadError}
import cats.effect.Sync
import io.es._
import io.es.algebra._
import cats.implicits._
import cats.mtl.{FunctorTell, MonadState}
import fs2.async.Ref
import io.es.infra.Repository._
import io.es.infra.data.{Aggregate, EventFormat, RawEvent}

class Repository[S, E](aggregate: Aggregate[S, E], format: EventFormat[E]) {

  private class AggregateState[F[_]](ref: Ref[F, S])(implicit sync: Sync[F]) extends MonadState[F, S] {
    override val monad: Monad[F] = sync

    override def get: F[S] = ref.get

    override def set(s: S): F[Unit] = ref.setSync(s)

    override def inspect[A](f: S => A): F[A] = ref.get.map(f)

    override def modify(f: S => S): F[Unit] = ref.modify(f) *> sync.unit
  }

  private class ApplySourceLog[F[_]](val ref: Ref[F, SourceResult[S, E]])(implicit sync: Sync[F]) extends FunctorTell[F, SourceLog[S, E]] {
    override val functor: Functor[F] = sync

    override def tell(l: SourceLog[S, E]): F[Unit] = {
      ref.modify(result =>
        result.copy(logs = l :: result.logs)
      ) *> sync.unit
    }

    override def writer[A](a: A, l: SourceLog[S, E]): F[A] = tell(l) *> sync.pure(a)

    override def tuple[A](ta: (SourceLog[S, E], A)): F[A] = tell(ta._1) *> sync.pure(ta._2)
  }

  def save[F[_]: Journal: SourceLogger](source: Source[S, E])(implicit sync: Sync[F]): F[UUID] =  {
    val logsRef = for {
      logs <- Ref[F, SourceResult[S, E]](SourceResult(Nil))
    } yield new ApplySourceLog[F](logs)

    logsRef.flatMap { implicit logs =>
      for {
        id <- saveWithLogs[F](source)
        result <- logs.ref.get
        _ <- SourceLogger[F].logResult(result)(aggregate, format)
      } yield id
    }
  }

  private def saveWithLogs[F[_]: Journal: SourceLogger](source: Source[S, E])(implicit sync: Sync[F], W: FunctorTell[F, SourceLog[S, E]]): F[UUID] =  {
    source match {
      case Source(NewSource(firstEventResult), PartialSource(list)) =>

      runCreateSource[F](firstEventResult).flatMap { case (aggr, firstEvent) =>

        val stateRef = for {
          state <- Ref[F, S](aggr)
        } yield new AggregateState[F](state)

        stateRef.flatMap { implicit state =>
          for {
            events <- list.traverse[F, E](runSource[F]).map(list => firstEvent :: list)
            rawEvents = events.map(e => RawEvent(format.encode(e)))
            uuid <- Journal[F].create(aggregate.id(aggr), aggregate.tag)(rawEvents)
          } yield uuid
        }
      }

      case Source(HydratedSource(id), PartialSource(list)) =>
        Journal[F].update(id, aggregate.tag) { stream =>
          val stateRef = for {
            aggr <- evalStream[F](id, stream)
            _ <- W.tell(SuccessHydrateSourceLog(aggr))
            state <- Ref[F, S](aggr)
          } yield new AggregateState[F](state)

          stateRef.flatMap { implicit state =>
            for {
              events <- list.traverse[F, E](runSource[F])
              rawEvents = events.map(e => RawEvent(format.encode(e)))
            } yield rawEvents
          }
        }
    }
  }

  private def evalStream[F[_]](id: UUID, stream: fs2.Stream[F, RawEvent])(implicit sync: Sync[F]): F[S] = {
    stream
      .evalMap(decodeEvent[F])
      .evalScan(Option.empty[S]) { case (s, e) =>
        aggregate.handle(s)(e) match {
          case Some(aggr) => sync.pure(Some(aggr))
          case None => sync.raiseError(HydrateUncaughtEventError(s.map(_.toString), e.toString))
        }
      }
      .compile.last.map(_.flatten)
      .flatMap {
        case Some(s) => sync.pure(s)
        case None => sync.raiseError[S](HydrateNoAggregateFound(id))
      }
  }

  private def decodeEvent[F[_]](rawEvent: RawEvent)(implicit ME: MonadError[F, Throwable]): F[E] = {
    format.decode(rawEvent) match {
      case Right(e) => ME.pure(e)
      case Left(err) => ME.raiseError(HydrateDecodeEventError(rawEvent.payload, err))
    }
  }

  private def runSource[F[_]](toApply: S => Result[E])
                     (implicit MS: MonadState[F, S], ME: MonadError[F, Throwable], W: FunctorTell[F, SourceLog[S, E]]): F[E] = {
      for {
        state <- MS.get
        event <- toApply(state) match {
          case Right(e) =>
            ME.pure(e)
          case Left(strErr) =>
            W.tell(ErrorUpdateSourceLog(state, strErr)) *>
              ME.raiseError(SourceApplyEventError(strErr))
        }
        _ <- aggregate.handle(Some(state))(event) match {
          case Some(newState) =>
            W.tell(SuccessUpdateSourceLog(newState, event)) *>
              MS.set(newState)
          case None =>
            W.tell(ErrorUncaughtEventSourceLog(Some(state), event)) *>
              ME.raiseError(SourceUncaughtEventError)
        }
      } yield event
  }

  private def runCreateSource[F[_]](toApply: Result[E])(implicit ME: MonadError[F, Throwable], W: FunctorTell[F, SourceLog[S, E]]): F[(S, E)] = {
    toApply match {
      case Right(event) =>
        aggregate.handle(None)(event) match {
          case Some(newState) =>
            W.tell(SuccessCreateSourceLog(newState, event)) *>
              ME.pure((newState, event))
          case None =>
            W.tell(ErrorUncaughtEventSourceLog(None, event)) *>
              ME.raiseError(SourceUncaughtEventError)
        }
      case Left(strErr) =>
        W.tell(ErrorCreateSourceLog(strErr)) *>
          ME.raiseError(SourceApplyEventError(strErr))
    }
  }
}

object Repository {

  sealed trait SourceLog[S, E]
  case class SuccessHydrateSourceLog[S, E](state: S) extends SourceLog[S, E]
  case class SuccessCreateSourceLog[S, E](state: S, event: E) extends SourceLog[S, E]
  case class SuccessUpdateSourceLog[S, E](state: S, event: E) extends SourceLog[S, E]
  case class ErrorCreateSourceLog[S, E](error: String) extends SourceLog[S, E]
  case class ErrorUpdateSourceLog[S, E](state: S, error: String) extends SourceLog[S, E]
  case class ErrorUncaughtEventSourceLog[S, E](state: Option[S], event: E) extends SourceLog[S, E]

  case class SourceResult[S, E](logs: List[SourceLog[S, E]])

  def newSourceResult[S, E](log: SourceLog[S, E]): SourceResult[S, E] = {
    SourceResult(log :: Nil)
  }
}