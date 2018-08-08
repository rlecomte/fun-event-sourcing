package io.es

import io.es.domain.turtle.{Turtle, TurtleEvent}
import io.es.domain.turtle.model._
import io.es.example.turtle.TurtleEvent
import io.es.infra.{PartialSource, Source}
import org.scalatest.{FlatSpec, Matchers}

class TurtleSpec extends FlatSpec with Matchers {

  "The V8 object" should "be valid" in {
    //implicit val sourceIntepreter: SourceInterpreter[IO] = IOSourceInterpreter

    import io.es.free._

    journal.hydrate(java.util.UUID.randomUUID()).flatMap { _ =>


    }

    def walkRight[F[_]](dist: Int): PartialSource[Turtle, TurtleEvent] = {
      Source
        .partial(Turtle.turn(ToRight))
        .andThen(Turtle.walk(dist)(_))
    }

    val source = Source
      .create[Turtle, TurtleEvent](Turtle.create("1", Position(0, 1), North))
      .andThen(walkRight(2))
      .andThen(walkRight(1))


    trait Aggregate[A] {
      type Event
      def tag: String
    }

    object Aggregate {
      type Aux[A, E] = Aggregate[A] { type Event = E }

      def define[A, E](tag: String): Aggregate.Aux[A, E] = new Aggregate[A] {
        type Event = E

        override def tag: String = tag
      }

      def fromAggregate[A] = {

      }
    }


    /*events shouldBe List(
      TurtleCreated("1", Position(0, 1), North),
      TurtleDirectionChanged("1", ToRight),
      TurtleMoved("1", 2),
      TurtleDirectionChanged("1", ToRight),
      TurtleMoved("1", 1)
    )

    state shouldBe Turtle("1", Position(2, 0), South)*/
  }
}
