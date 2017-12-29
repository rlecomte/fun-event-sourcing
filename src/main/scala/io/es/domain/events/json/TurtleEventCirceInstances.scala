package io.es.domain.events.json

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}
import io.es.domain.turtle.Turtle.{Create, Turn, TurtleEvent, Walk}
import io.es.domain.turtle._

trait TurtleEventCirceInstances {

  //direction
  private val toNorthKey = "north"
  private val toSouthKey = "south"
  private val toEstKey = "est"
  private val toWestKey = "west"

  //rotation
  private val toLeftKey = "toLeft"
  private val toRightKey = "toRight"

  //position
  private val xKey = "x"
  private val yKey = "y"

  implicit val encoderDirection: Encoder[Direction] = Encoder[String].contramap[Direction] {
    case North => toNorthKey
    case South => toSouthKey
    case Est   => toEstKey
    case West  => toWestKey
  }

  implicit val decoderDirection: Decoder[Direction] = Decoder[String].emap {
    case `toNorthKey` => Right(North)
    case `toSouthKey` => Right(South)
    case `toEstKey`   => Right(Est)
    case `toWestKey`  => Right(West)
    case _ => Left("Unexpected direction.")
  }

  implicit val encoderPosition: Encoder[Position] = Encoder.instance { p =>
    Json.obj(
      xKey -> p.x.asJson,
      yKey -> p.y.asJson
    )
  }

  implicit val decoderPosition: Decoder[Position] = Decoder.instance { c =>
    for {
      x <- c.get[Int](xKey)
      y <- c.get[Int](yKey)
    } yield Position(x, y)
  }

  implicit val encoderRotation: Encoder[Rotation] = Encoder[String].contramap {
    case ToLeft => toLeftKey
    case ToRight => toRightKey
  }

  implicit val decoderRotation: Decoder[Rotation] = Decoder[String].emap {
    case `toLeftKey` => Right(ToLeft)
    case `toRightKey` => Right(ToRight)
    case _ => Left("Unexpected rotation.")
  }

  implicit val createEventDecoder: Decoder[Create] = deriveDecoder

  implicit val turnEventDecoder: Decoder[Turn] = deriveDecoder

  implicit val walkEventDecoder: Decoder[Walk] = deriveDecoder

  implicit val turtleEventDecoder: Decoder[TurtleEvent] = deriveDecoder

  implicit val createEventEncoder: Encoder[Create] = deriveEncoder

  implicit val turnEventEncoder: Encoder[Turn] = deriveEncoder

  implicit val walkEventEncoder: Encoder[Walk] = deriveEncoder

  implicit val turtleEventEncoder: Encoder[TurtleEvent] = deriveEncoder
}