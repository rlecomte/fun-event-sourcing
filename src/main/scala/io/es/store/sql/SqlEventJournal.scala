package io.es.store.sql

import java.time.ZonedDateTime

import cats.data.NonEmptyList
import cats.effect.IO
import doobie.util.transactor.Transactor
import io.circe.Json
import io.es.UUID
import io.es.infra._
import io.es.infra.data._

class SqlEventJournal(xa: Transactor[IO]) extends EventJournal[Json] with DoobieMetaInstances {

  import cats.effect.IO
  import cats.implicits._
  import doobie._
  import doobie.implicits._
  import doobie.postgres.implicits._

  override def write[S <: Aggregate, E <: Event](aggregateId: AggregateId, originatingVersion: Version, events: NonEmptyList[E])
    (implicit aggregate: AggregateTag[S, _, E], encoder: EventEncoder[E, Json]): IO[Unit] = {

    val selectVersionOfAggregate: ConnectionIO[Option[Version]] = {
      val sql: Query0[Version] = {
        sql"""
              SELECT version FROM aggregates where id = $aggregateId
          """.query[Version]
      }

      sql.option
    }

    val insertNewAggregate: ConnectionIO[Unit] = {
      val sql: Update0 =
        sql"""
               INSERT INTO aggregates (id, aggregate_type, version) VALUES ($aggregateId, ${aggregate.aggregateType}, 0)
            """
          .update
      sql.run.map(_ => ())
    }

    def updateAggregate(newVersion: Version): ConnectionIO[Unit] = {
      val sql: Update0 =
        sql"""
              UPDATE aggregates SET version = $newVersion where id = $aggregateId
          """
          .update
      sql.run.map(_ => ())
    }

    val insertEvents = s"INSERT INTO events (aggregate_id, version, data, aggregate_type) VALUES (?, ?, ?, ?)"

    val query = for {
      aggregateVersion <- selectVersionOfAggregate

      _ <- aggregateVersion match {
        case Some(v) =>
          if (v != originatingVersion) (ConcurrencyEventException: Throwable).raiseError[ConnectionIO, Unit]
          else ().pure[ConnectionIO]
        case None =>
          insertNewAggregate
      }

      _ <- {

        val lastVersion = originatingVersion.value + events.length

        val incVersions = Stream.range(originatingVersion.inc.value, lastVersion + 1L).map(Version.apply)

        val eventsWithVersion = events.toList.zip(incVersions)
          .map { case (e, v) => encoder(aggregateId, v, ZonedDateTime.now(), e) }

        Update[(UUID, Version, Json, String)](insertEvents)
          .updateMany(eventsWithVersion.map(e => (e.aggregateId, e.version, e.data, e.aggregateType)))
          .flatMap(_ => updateAggregate(Version(lastVersion)))
      }
    } yield ()

    query.transact(xa)
  }

  override def hydrate[S <: Aggregate, E <: Event](aggregateId: AggregateId)
    (implicit handler: EventHandler[S, E], aggregate: AggregateTag[S, _, E], jsonEventDecoder: EventDecoder[E, Json]): IO[Option[(S, Version)]] = {
    val sql: Query0[RawEvent[Json]] = sql"""
       SELECT aggregate_id, version, seq_number, data, date_event, aggregate_type
       FROM events
       WHERE aggregate_id=$aggregateId
       ORDER BY version
    """.query[RawEvent[Json]]

    sql.process
      .map { rawEvent => (jsonEventDecoder(rawEvent), rawEvent.version) }
      .fold[Option[(S, Version)]](None) { case (s, (e, v)) => Some((handler(s.map(_._1), e), v)) }
      .list
      .transact(xa)
      .map(_.headOption.flatten)
  }
}
