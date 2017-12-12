package io.es

import doobie.hikari.HikariTransactor
import io.es.infra.Sourced.sourceNew
import io.es.store.sql.SqlEventJournal
import io.es.turtle.{North, Position, Turtle}

object Main extends App {

  import doobie._, doobie.implicits._
  import cats._, cats.data._, cats.effect.IO, cats.implicits._

  import io.es.store.sql.events._

  val xa = HikariTransactor[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://127.0.0.1:5432/world",
    "postgres",
    null
  ).unsafeRunSync()

  DbBootstrap.migrate()

  val journal = new SqlEventJournal(xa)

  val turtle = sourceNew(Turtle.create("1", Position(0, 1), North)).andThen[Unit] {
    for {
      _ <- Turtle.walkRight(2)
      _ <- Turtle.walkRight(1)
    } yield ()
  }

  journal.write(java.util.UUID.randomUUID(), 0L, turtle.events.right.get).unsafeRunSync()

  /*
    trait Command {
      type S: Aggregate
      val aggregateId: Option[UUID]
    }

    val journal = {
      def hydrate[S](id: UUID): IO[Option[S]]
      def persist(event: E): IO[Unit]
    }

    val handlers: List[CommandHandler] = ???

    handle[Cmd](cmd: Cmd) = {
      val optState = cmd.aggregateId.flatMap(hydrate)
      handlers.find(_isDefinedAt((cmd, optState))) match {
        case Some(h) =>
          val result = h((cmd, optState)).runUnsafeSync

        case None =>
      }
    }

    persist {
      // security
      // log
      // PartialFunction[Cmd, IO[CreatedSource[State, Event, Unit]]]

      CommandHandler extends PartialFunction[(Cmd, Option[S]), CreatedSource[S, E, Unit]] {
        case FooCmd => ???
      }


    }

   */
}
