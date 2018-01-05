package io.es

import com.typesafe.config.Config

case class SqlStoreConfig(
  driver: String,
  url: String,
  user: String,
  password: Option[String]
)

object SqlStoreConfig {

  def apply(config: Config): SqlStoreConfig = {
    import pureconfig.loadConfig
    loadConfig[SqlStoreConfig].right.get
  }
}
