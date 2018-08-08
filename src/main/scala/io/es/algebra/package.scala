package io.es

import cats.free.Free

package object algebra {
  type Stack[A] = Free[JournalOps, A]

  val journal: Journal[JournalOps] = new Journal[JournalOps]
}
