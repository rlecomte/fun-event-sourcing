package io.es

import com.typesafe.config.ConfigFactory

import cats.effect.IO
import io.es.domain.turtle.Turtle._
import io.es.domain.turtle.{North, Position}
import io.es.infra.data.{AggregateId, CommandResult}
import io.es.store.sql.SqlEventJournal

object Main extends App {

  import io.es.domain._

  val config = ConfigFactory.load()
  val sqlStoreConfig = SqlStoreConfig(config.atKey("store.sql"))

  object infra {

    val dbBootstrap = DbBootstrap(sqlStoreConfig)

    val xa = SqlTransactor.hikari(sqlStoreConfig)

    val journal = SqlEventJournal(xa)

    val domainHandler = mainSystem(journal)
  }

  def readAndExecuteCommand(line: String): IO[CommandResult] = {

    val id = AggregateId(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"))

    def commandParser(value: String): Option[TurtleCommand] = value match {
      case "create" => Some(CreateCmd(pos = Position.zero, dir = North))
      case "right" => Some(WalkRightCmd(id, 1))
      case "left" => Some(WalkLeftCmd(id, 1))
      case _ => Some(WalkCmd(id, 1))
    }

    commandParser(line) match {
      case Some(cmd) => infra.domainHandler(cmd)
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


  infra.dbBootstrap.migrate()
  program.unsafeRunSync()
}
