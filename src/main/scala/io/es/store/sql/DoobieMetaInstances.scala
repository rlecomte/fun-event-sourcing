package io.es.store.sql

import org.postgresql.util.PGobject

import doobie.util.meta.Meta
import io.circe.Json

trait DoobieMetaInstances {

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
}
