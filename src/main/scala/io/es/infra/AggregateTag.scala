package io.es.infra

import io.es.infra.data.{Aggregate, Command, Event}

trait AggregateTag[A <: Aggregate] {
  type Id <: String

  type AggregateEvent <: Event

  type AggregateCommand <: Command

  def aggregateType: String
}

object AggregateTag {
  type Aux[A <: Aggregate, C <: Command, E <: Event] = AggregateTag[A] {
    type AggregateEvent = E
    type AggregateCommand = C
  }

  def apply[A <: Aggregate, C <: Command, E <: Event](tag: String): AggregateTag.Aux[A, C, E] = new AggregateTag[A] {
    type AggregateEvent = E

    type AggregateCommand = C

    override def aggregateType: String = tag
  }
}
