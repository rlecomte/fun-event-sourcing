package io.es.infra.data

import shapeless.HList
import shapeless.LUBConstraint.<<:
import shapeless.LUBConstraint._

case class Domain[D <: HList : <<:[Aggregate]#λ]()
