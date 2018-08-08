package io.es.test

import cats.Id
import io.es.UUID
import io.es.infra.{EventFormat, Journal}

class InMemoryJournal extends Journal[Id] {

  var map: collection.mutable.Map[UUID, Seq[Any]] = collection.mutable.Map()

  override def hydrate[E](id: UUID)(implicit format: EventFormat[E]): fs2.Stream[Id, E] = {
    fs2.Stream(map.get(id)).map(_.asInstanceOf[E])
  }

  override def register[E](key: String, id: UUID, payload: Seq[E])(implicit format: EventFormat[E]): Id[Unit] = {

  }
}
