package io.es.infra

import cats.Eval
import cats.data.ReaderWriterStateT
import cats.implicits._

object Sourced {

  def sourceNew[STATE, EVENT](init: Result[EVENT])(implicit handler: EventHandler[STATE, EVENT]): CreateSource[STATE, EVENT, Unit] = {
    CreateSource(
      init.map(event => (handler(None, event), event)),
      UpdateSource(
        ReaderWriterStateT.pure[Result, Unit, List[EVENT], STATE, Unit](())
      )
    )
  }

  def source[STATE, EVENT](f: STATE => Result[EVENT])(implicit handler: EventHandler[STATE, EVENT]): UpdateSource[STATE, EVENT, Unit] = {
    UpdateSource(for {
      event <- ReaderWriterStateT.inspectF[Result, Unit, List[EVENT], STATE, EVENT](f)
      _ <- ReaderWriterStateT.modify[Result, Unit, List[EVENT], STATE](s => handler(Some(s), event))
      _ <- ReaderWriterStateT.tell[Result, Unit, List[EVENT], STATE](List(event))
    } yield ())
  }

  case class CreateSource[STATE, EVENT, A](init: Result[(STATE, EVENT)], next: UpdateSource[STATE, EVENT, A]) {
    val values: Eval[Result[(List[EVENT], STATE, A)]] = Eval.later {
      init.flatMap { case (state, event) =>
          val r = next.sourced.run((), state)
          r.map { case (e, s, a) => (event +: e, s, a)}
      }
    }

    def events: Result[List[EVENT]] = values.value.map(_._1)

    def state: Result[STATE] = values.value.map(_._2)

    def value: Result[A] = values.value.map(_._3)

    def andThen[B](f: UpdateSource[STATE, EVENT, B]): CreateSource[STATE, EVENT, B] = {
      CreateSource(init, next.flatMap(_ => f))
    }
  }

  case class UpdateSource[STATE, EVENT, A](sourced: Sourcing[STATE, EVENT, A]) {

    def andThen[B](f: Sourcing[STATE, EVENT, B]): UpdateSource[STATE, EVENT, B] = {
      UpdateSource(sourced.flatMap(_ => f))
    }

    def map[B](f: A => B): UpdateSource[STATE, EVENT, B] = {
      flatMap[B] { a =>
        UpdateSource(ReaderWriterStateT.pure[Result, Unit, List[EVENT], STATE, B](f(a)))
      }
    }

    def flatMap[B](f: A => UpdateSource[STATE, EVENT, B]): UpdateSource[STATE, EVENT, B] = {
      UpdateSource(sourced.flatMap(a => f(a).sourced))
    }
  }
}
