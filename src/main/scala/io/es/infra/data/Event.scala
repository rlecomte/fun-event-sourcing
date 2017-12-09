package io.es.infra.data

trait Event[E, P] {
  def toStore(e: E): P
  def fromStore(payload: P): E
  def aggregateType: String
}
