package io.es

import cats.effect.IO
import doobie.hikari.HikariTransactor
import io.es.domain.turtle.Turtle.{CreateCmd, TurtleCommand, WalkCmd, WalkLeftCmd, WalkRightCmd}
import io.es.domain.turtle.{North, Position, TurtleCommandHandler}
import io.es.infra.data.{AggregateId, CommandResult}
import io.es.store.sql.SqlEventJournal

object Main extends App {

  import io.es.domain._

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

    val id = AggregateId(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"))

    def commandParser(value: String): Option[TurtleCommand] = value match {
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
