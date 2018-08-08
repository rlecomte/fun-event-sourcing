package io.es.algebra

import io.es.algebra.model.{Bearer, User}

trait Auth[F[_]] {
  def auth(bearer: Bearer): F[User]
}
