package io.es.infra.data

trait Command {
  def aggregateId: Option[AggregateId]
}