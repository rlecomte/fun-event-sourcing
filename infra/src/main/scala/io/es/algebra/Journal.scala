package io.es.algebra

import io.es.UUID
import io.es.infra.data.{RawEvent, Tag}

/**
  * This is the endpoint of the EventStore. Journal allow to create and update
  * aggregate in the EventStore in an atomic fashion.
  * @tparam F
  */
trait Journal[F[_]] {

  /**
    * Update an aggregate. This operation have to be atomic for each aggregate.
    * @param id the aggregate id
    * @param key tag of the kind of aggregate
    * @param f pure function which return the list of new events applied on the aggregate
    *          and that have to be register on the event store
    * @return
    */
  def update(id: UUID, key: Tag)(f: fs2.Stream[F, RawEvent] => F[List[RawEvent]]): F[UUID]

  /**
    * Create a new aggregate.
    * @param id the new aggregate id
    * @param key tag of the kind of aggregate
    * @param events the list of events to be registered
    * @return
    */
  def create(id: UUID, key: Tag)(events: List[RawEvent]): F[UUID]
}

object Journal {
  def apply[F[_]](implicit journal: Journal[F]): Journal[F] = journal
}
