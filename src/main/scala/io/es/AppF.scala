package io.es

import io.es.algebra.Auth
import io.es.infra.Journal

abstract class AppF[F[_]] {

  val auth: Auth[F]

  val journal: Journal[F]
}
