package io.es

import org.scalatest.{FlatSpec, Matchers}

import io.es.turtle._
import io.es.turtle.Turtle.{Create, Turn, TurtleEvent, Walk}

class TurtleSpec extends FlatSpec with Matchers {
  import io.es.infra.Sourced._

  "The V8 object" should "be valid" in {

    val tested = sourceNew(Turtle.create("1", Position(0, 1), North)).andThen[Unit] {
      for {
        _ <- Turtle.walkRight(2)
        _ <- Turtle.walkRight(1)
      } yield ()
    }

    tested.events shouldBe Right(Vector(
      Create("1", Position(0, 1), North),
      Turn("1", ToRight),
      Walk("1", 2),
      Turn("1", ToRight),
      Walk("1", 1)
    ))

    tested.state shouldBe Right(Turtle("1", Position(2, 0), South))
  }
}
