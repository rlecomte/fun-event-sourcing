package io.es.infra

import io.es.infra.data.{Aggregate, Command, Event}

trait AggregateTag[A <: Aggregate, C <: Command, E <: Event] {
  def aggregateType: String
}

object AggregateTag {
  def apply[A <: Aggregate, C <: Command, E <: Event](tag: String): AggregateTag[A, C, E] = new AggregateTag[A, C, E] {
    override def aggregateType: String = tag
  }
}
