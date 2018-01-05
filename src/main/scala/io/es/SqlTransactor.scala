package io.es

import cats.effect.IO
import doobie.hikari.HikariTransactor

object SqlTransactor {

  def hikari(config: SqlStoreConfig) = {
    HikariTransactor[IO](
      config.driver,
      config.url,
      config.user,
      config.password.orNull
    ).unsafeRunSync()
  }
}
