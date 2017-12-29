package io.es.store.sql

import java.time.ZonedDateTime

import cats.effect.IO
import doobie.util.transactor.Transactor
import io.circe.Json
import io.es.UUID
import io.es.infra.EventDecoder.EventDecoder
import io.es.infra.EventEncoder.EventEncoder
import io.es.infra.data._
import io.es.infra.{AggregateTag, ConcurrencyEventException, EventHandler, EventJournal}

class SqlEventJournal(xa: Transactor[IO]) extends EventJournal[Json] with DoobieMetaInstances {

  import cats.effect.IO
  import cats.implicits._
  import doobie._
  import doobie.implicits._
  import doobie.postgres.implicits._

  override def write[S <: Aggregate, E <: Event](aggregateId: UUID, originatingVersion: Long, events: List[E])
    (implicit aggregate: AggregateTag.Aux[S, _, E], encoder: EventEncoder[E, Json]): IO[Unit] = {
    (for {
      _ <- failIfConflict(aggregateId, originatingVersion)
      _ <- writeEvents(aggregateId, originatingVersion, events)
    } yield ()).transact(xa)
  }


  override def hydrate[S <: Aggregate, E <: Event](aggregateId: UUID)
    (implicit handler: EventHandler[S, E], aggregate: AggregateTag.Aux[S, _, E], jsonEventDecoder: EventDecoder[E, Json]): IO[Option[(S, Long)]] = {
    val sql: Query0[RawEvent[Json]] = sql"""
       SELECT aggregate_id, version, data, date_event, aggregate_type
       FROM events
       WHERE aggregate_id=$aggregateId
       ORDER BY version
    """.query[RawEvent[Json]]

    sql.process
      .map { rawEvent => (jsonEventDecoder(rawEvent), rawEvent.version) }
      .fold[Option[(S, Long)]](None) { case (s, (e, v)) => Some((handler(s.map(_._1), e), v)) }
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

  private def writeEvents[S <: Aggregate, E <: Event](aggregateId: UUID, originatingVersion: Long, events: List[E])
    (implicit aggregate: AggregateTag.Aux[S, _, E], encoder: EventEncoder[E, Json]): ConnectionIO[Unit] = {

    val insertAggregate: ConnectionIO[Unit] = {
      if (originatingVersion < 1) {
        val sql: Update0 =
          sql"""
               INSERT INTO aggregates (id, aggregate_type, version) VALUES ($aggregateId, ${aggregate.aggregateType}, 0)
            """
          .update
        sql.run.map(_ => ())
      } else {
        ().pure[ConnectionIO]
      }
    }

    val increments = Stream.iterate(originatingVersion + 1)(_ + 1).take(events.size + 1).toList
    val tuples = events.zip(increments).map { case (e, v) => encoder(AggregateId(aggregateId), Version(v), ZonedDateTime.now(), e) }

    val sql = "INSERT INTO events (aggregate_id, version, data) VALUES (?, ?, ?)"

    for {
      _ <- insertAggregate
      _ <- Update[RawEvent[Json]] (sql).updateMany(tuples)
    } yield ()
  }

}
