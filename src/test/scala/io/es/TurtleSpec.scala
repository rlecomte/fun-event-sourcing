package io.es

import org.scalatest.{FlatSpec, Matchers}

import io.es.domain.turtle._
import io.es.domain.turtle.Turtle.{Create, Turn, Walk}
import io.es.infra.data.AggregateId

class TurtleSpec extends FlatSpec with Matchers {
  import io.es.infra.Sourced._

  val id = AggregateId.zero

  "The V8 object" should "be valid" in {

    val tested = sourceNew(Turtle.create(id, Position(0, 1), North)).andThen[Unit] {
      for {
        _ <- Turtle.walkRight(2)
        _ <- Turtle.walkRight(1)
      } yield ()
    }

    tested.events shouldBe Right(Vector(
      Create(id, Position(0, 1), North),
      Turn(id, ToRight),
      Walk(id, 2),
      Turn(id, ToRight),
      Walk(id, 1)
    ))

    tested.state shouldBe Right(Turtle(id, Position(2, 0), South))
  }
}
