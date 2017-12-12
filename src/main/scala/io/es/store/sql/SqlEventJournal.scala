package io.es.store.sql

import cats.effect.IO
import doobie.util.transactor.Transactor
import io.circe.Json
import io.es.UUID
import io.es.infra.data.Event
import io.es.infra.{ConcurrencyEventException, EventHandler, EventJournal}

class SqlEventJournal(xa: Transactor[IO]) extends EventJournal[Json] with DoobieMetaInstances {

  import cats.effect.IO
  import cats.implicits._
  import doobie._
  import doobie.implicits._
  import doobie.postgres.implicits._

  override def write[EVENT](aggregateId: UUID, originatingVersion: Long, events: List[EVENT])
    (implicit event: Event[EVENT, Json]): IO[Unit] = {
    (for {
      _ <- failIfConflict(aggregateId, originatingVersion)
      _ <- writeEvents(aggregateId, originatingVersion, events)
    } yield ()).transact(xa)
  }


  override def hydrate[STATE, EVENT](aggregateId: UUID)
    (implicit handler: EventHandler[STATE, EVENT], event: Event[EVENT, Json]): IO[Option[(STATE, Long)]] = {
    val sql: Query0[(Json, Long)] = sql"""
       SELECT data, version
       FROM events
       WHERE aggregate_id=$aggregateId
       ORDER BY version
    """.query[(Json, Long)]

    sql.process
      .map { case (data, version) => (event.fromStore(data), version) }
      .fold[Option[(STATE, Long)]](None) { case (s, (e, v)) => Some((handler(s.map(_._1), e), v)) }
      .list
      .transact(xa)
      .map(_.headOption.flatten)
  }

  private def failIfConflict(aggregateId: UUID, originatingVersion: Long): ConnectionIO[Unit] = {

    val sql: Query0[Boolean] =
      sql"""
        SELECT 1
        FROM events
        WHERE
          aggregate_id=($aggregateId :: uuid) AND
          version > $originatingVersion
        """
        .query[Boolean]

    for {
      isConcurrentAccess <- sql.list.map(_.headOption.getOrElse(false))
      _ <- HC.delay(if (isConcurrentAccess) throw ConcurrencyEventException else ())
    } yield ()
  }

  private def writeEvents[E](aggregateId: UUID, originatingVersion: Long, events: List[E])(implicit event: Event[E, Json]): ConnectionIO[Unit] = {

    val insertAggregate: ConnectionIO[Unit] = {
      if (originatingVersion < 1) {
        val sql: Update0 =
          sql"""
               INSERT INTO aggregates (id, aggregate_type, version) VALUES ($aggregateId, ${event.aggregateType}, 0)
            """
          .update
        sql.run.map(_ => ())
      } else {
        ().pure[ConnectionIO]
      }
    }

    val increments = Stream.iterate(originatingVersion + 1)(_ + 1).take(events.size + 1).toList
    val tuples = events.zip(increments).map { case (e, v) => (aggregateId, v, event.toStore(e)) }

    val sql = "INSERT INTO events (aggregate_id, version, data) VALUES (?, ?, ?)"

    for {
      _ <- insertAggregate
      _ <- Update[(UUID, Long, Json)] (sql).updateMany(tuples)
    } yield ()
  }

}
