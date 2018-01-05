package io.es.store.sql

import java.sql.Timestamp
import java.time.{ZoneId, ZonedDateTime}

import org.postgresql.util.PGobject

import doobie.util.meta.Meta
import io.circe.Json
import io.es.UUID
import io.es.infra.data.{AggregateId, SequenceNumber, Version}

trait DoobieMetaInstances {

  import doobie.postgres.implicits._

  implicit val JsonMeta: Meta[Json] =
    Meta.other[PGobject]("json").xmap[Json](
      a => io.circe.parser.parse(a.getValue).left.map[Json](e => throw e).merge, // failure raises an exception
      a => {
        val o = new PGobject
        o.setType("json")
        o.setValue(a.noSpaces)
        o
      }
    )

  implicit val VersionMeta: Meta[Version] = Meta[Long].xmap(Version.apply, _.value)

  implicit val SequenceNumberMeta: Meta[SequenceNumber] = Meta[Long].xmap(SequenceNumber.apply, _.value)

  implicit val ZonedDateTimeMeta: Meta[ZonedDateTime] = Meta[java.sql.Timestamp].xmap(
    ts => ts.toLocalDateTime.atZone(ZoneId.systemDefault()),
    zdt =>Timestamp.from(zdt.toInstant)
  )

  implicit val AggregateIdMeta: Meta[AggregateId] = Meta[UUID].xmap(AggregateId.apply, _.value)
}
