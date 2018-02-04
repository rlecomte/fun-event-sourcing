package io.es.store.sql

import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

import cats.data.NonEmptyList
import io.es.domain.turtle.{North, Position, Turtle}
import io.es.infra.data.{AggregateId, Version}
import io.es.{DbBootstrap, SqlStoreConfig, SqlTransactor}

class SqlEventJournalSpec extends FlatSpec with Matchers with BeforeAndAfterEach {

  var journal: SqlEventJournal = null

  override protected def beforeEach(): Unit = {

    val config = SqlStoreConfig(
      "org.postgresql.Driver",
      "jdbc:postgresql://127.0.0.1:5432/world",
      "postgres",
      None
    )

    DbBootstrap(config).migrate()

    val xa = SqlTransactor.hikari(config)
    journal = new SqlEventJournal(xa)
  }

  override protected def afterEach(): Unit = {
  }

  "The SQL event journal" should "create new aggregate" in {
    import io.es.domain._

    val id = AggregateId.newId()
    val createEvent = Turtle.create(
      id,
      Position.zero,
      North
    ).right.get


    //journal.write()(id, Version.origin, NonEmptyList.one(createEvent)).unsafeRunSync()
  }
}
