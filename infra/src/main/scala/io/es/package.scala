package io

package object es {
  type UUID      = java.util.UUID
  type Result[A] = Either[String, A]
}
