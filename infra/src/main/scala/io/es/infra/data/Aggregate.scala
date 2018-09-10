package io.es.infra.data

import java.util.UUID

/**
  * Aggregate is a typeclass providing hability to define Aggregate instance for a type `A`
  * and that can handle a type of event `E`
  * @tparam A the type of the aggregate
  * @tparam E the type of the aggregate's events
  */
trait Aggregate[A, E] {

  /**
    * the Tag is the name of the aggregate. This value should be unique for each type of aggregate.
    * @return the aggregate's tag
    */
  def tag: Tag

  /**
    * extract the UUID of an aggregate data structure
    * @param aggregate
    * @return the aggregate's id
    */
  def id(aggregate: A): UUID

  /**
    * Handle an event and create a new version of the aggregate after applying the event.
    * This function return an Option[A] to be handle error case. If the combination of Option[A] with
    * event E is not allowed then return None.
    * @param aggregate the initial value of the aggregate. None mean that is a new aggregate.
    * @param event the event to apply on aggregate
    * @return the new version of the aggregate or None if failed to handle event.
    */
  def handle(aggregate: Option[A])(event: E): Option[A]
}
