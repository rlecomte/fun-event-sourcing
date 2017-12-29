package io.es.infra.data

/*trait Ev[E] {
  type Id <: String
}

object Ev {
  type Aux[E, T] = Ev[E] { type Id = T }

  def apply[E](v: String): Ev.Aux[E, v.type] = new Ev[E] { type Id = v.type }
}

trait ShowId[E] {
  def show[T <: String](implicit ev: Ev.Aux[E, T], v: ValueOf[T]): String = valueOf[T]
}


case class RawEvent[P](payload: P, id: String)


object EvDecoder {

  type EvDecoder[E, P] = PartialFunction[RawEvent[P], E]
}

object JsonEvDecoder {
  type JsonEvDecoder[E] = EvDecoder[E, Json]

  def apply[E, T <: String](implicit ev: Ev.Aux[E, T], v: ValueOf[T], decoder: Decoder[E]): EvDecoder[E, Json] = new PartialFunction[RawEvent[Json], E] {
    override def isDefinedAt(event: RawEvent[Json]) = valueOf[T] == event.id

    override def apply(event: RawEvent[Json]) = decoder.decodeJson(event.payload).right.get
  }
}

trait DomainEvent

case class Domain[D <: HList : <<:[DomainEvent]#λ]/*(
  implicit encoder: EvEncoder[D, P],
  implicit decoder: EvDecoder[D, P],
)*/ {
  def read[P](event: RawEvent[P])(implicit decoder: EvDecoder[D, P]): DomainEvent = ???
  //def write[E, P](event: RawEvent[P])(implicit decoder: EvEncoder[E, P]): RawEvent[P] = ???
}

object Domain {
  import shapeless.LUBConstraint._
  def apply[L <: HList : <<:[DomainEvent]#λ](l: L) = true
}
/*
  // Define your domain

  trait DomainAEvent extends DomainEvent
  ...

  trait DomainBEvent extends DomainEvent
  ...

  // Link all together

  implicit val domainAEvent = Ev[DomainAEvent]("domainA")

  implicit val domainBEvent = Ev[DomainBEvent]("domainB")

  type MyAppDomain = Domain["domainA" ->> DomainAEvent :: "domainB" ->> DomainBEvent :: HNil]

  // define json mapper
  implicit domainAJsonEvDecoder: JsonEvDecoder[DomainAEvent] = JsonEvDecoder()
  ...

 */

case class EventBus[D <: Domain[_], P](domain: D) {

  domain.read(null)

}*/