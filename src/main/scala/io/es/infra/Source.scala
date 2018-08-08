package io.es.infra

import io.es.Result
import io.es.infra.Source.RootSource

case class Source[S, E](
                         root: RootSource[S, E],
                         list: PartialSource[S, E] = PartialSource[S, E]()
                       ) {
  def andThen(f: S => Result[E]): Source[S, E] = Source(root, list.andThen(f))

  def andThen(f: PartialSource[S, E]): Source[S, E] = Source(root, list.andThen(f))
}

case class PartialSource[S, E](list: List[S => Result[E]] = Nil) extends AnyVal {

  def andThen(f: S => Result[E]): PartialSource[S, E] = PartialSource(f :: list)

  def andThen(f: PartialSource[S, E]): PartialSource[S, E] = PartialSource(f.list ++ list)
}

case class SourceResult[S, E](state: S, events: List[E])

object Source {
  def create[S, E](init: Result[E]): Source[S, E] = Source(NewSource(init))

  def hydrate[S, E](id: java.util.UUID): Source[S, E] = Source(HydratedSource(id))

  def partial[S, E](f: S => Result[E]): PartialSource[S, E] = PartialSource(f :: Nil)

  private[es] sealed trait RootSource[S, E]
  private[es] case class HydratedSource[S, E](id: java.util.UUID) extends RootSource[S, E]
  private[es] case class NewSource[S, E](init: Result[E]) extends RootSource[S, E]
}