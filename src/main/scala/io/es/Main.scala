package io.es

import doobie.hikari.HikariTransactor
import io.es.infra.data.CommandResult
import io.es.store.sql.SqlEventJournal
import io.es.turtle.{North, Position}
import io.es.turtle.Turtle.{CreateCmd, TurtleCmd, WalkCmd, WalkLeftCmd, WalkRightCmd}

object Main extends App {

  import cats.effect.IO
  import io.es.store.sql.events._
  import io.es.store.sql.aggregate._
  import io.es.store.sql.command._

  object infra {
    val xa = HikariTransactor[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql://127.0.0.1:5432/world",
      "postgres",
      null
    ).unsafeRunSync()

    val journal = new SqlEventJournal(xa)
  }

  def readAndExecuteCommand(line: String): IO[CommandResult] = {

    val id = java.util.UUID.fromString("00000000-0000-0000-0000-000000000000")
    def commandParser(value: String): Option[TurtleCmd] = value match {
      case "create" => Some(CreateCmd(Position.zero, North))
      case "right" => Some(WalkRightCmd(id, 1))
      case "left" => Some(WalkLeftCmd(id, 1))
      case _ => Some(WalkCmd(id, 1))
    }

    commandParser(line) match {
      case Some(cmd) => TurtleCommandHandler.turtleHandler.runCommand(cmd, infra.journal)
      case None => IO.raiseError(new RuntimeException("invalid command."))
    }
  }

  val program: IO[Unit] = for {
    _ <- IO(print("turtle > "))
    line <- IO(scala.io.StdIn.readLine())
    _ <- {
      if (line == "exit") IO.unit
      else {
        readAndExecuteCommand(line).attempt.flatMap {
          case Left(err) => IO(err.printStackTrace())
          case Right(_) => program
        }
      }
    }
  } yield ()


  DbBootstrap.migrate()
  program.unsafeRunSync()
}
