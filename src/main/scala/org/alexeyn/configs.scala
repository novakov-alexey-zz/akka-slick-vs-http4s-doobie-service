package org.alexeyn

import java.io.File

import com.typesafe.config.{Config, ConfigFactory, ConfigParseOptions}
import com.typesafe.scalalogging.StrictLogging
import pureconfig.error.ConfigReaderFailures
import pureconfig.loadConfig

case class Server(host: String = "localhost", port: Int = 8080)

object AppConfig extends StrictLogging {
  private val parseOptions = ConfigParseOptions.defaults().setAllowMissing(false)

  def load: Either[ConfigReaderFailures, (Server, Config)] = {
    val path = sys.env.getOrElse("APP_CONFIG_PATH", "src/main/resources/application.conf")
    val config = ConfigFactory.parseFile(new File(path), parseOptions).resolve()
    logger.debug("cfg: {}", config)
    val server = loadConfig[Server](config)
    server.map( _ -> config)
  }
}
