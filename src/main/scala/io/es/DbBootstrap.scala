package io.es

import org.flywaydb.core.Flyway

case class DbBootstrap(config: SqlStoreConfig) extends Flyway {
  setDataSource(config.url, config.user, config.password.orNull)
}

object DbBootstrap {

  def apply(config: SqlStoreConfig): Flyway = new Flyway {
    setDataSource(config.url, config.user, config.password.orNull)
  }

  def apply(url: String, user: String, password: String): Flyway = new Flyway {
    setDataSource(url, user, password)
  }
}
