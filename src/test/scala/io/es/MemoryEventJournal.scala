package io.es

import cats.Id
import io.es.data.{Aggregate, Event}

class MemoryEventJournal[STATE <: Aggregate, EVENT <: Event](
  implicit handler: EventHandler[STATE, EVENT]
) extends EventJournal[Id, STATE, EVENT] {
  private var journal = Seq.empty[EVENT]

  override def write(events: Seq[EVENT]): Id[Unit] = {
    synchronized { journal = journal ++ events }
  }

  override def hydrate(id: String) = {
    synchronized {
      journal.filter(_.id == id).toList match {
        case x :: xs => Some(xs.foldLeft(handler(None, x))((s, e) => handler(Some(s), e)))
        case Nil => None
      }
    }
  }
}
