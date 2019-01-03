package org.alexeyn

import java.io.File

import com.typesafe.config.{Config, ConfigFactory, ConfigParseOptions, ConfigRenderOptions}
import com.typesafe.scalalogging.StrictLogging
import pureconfig.generic.ProductHint
import pureconfig.error.ConfigReaderFailures
import pureconfig.{loadConfig, CamelCase, ConfigFieldMapping}
import pureconfig.generic.auto._
import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.string.MatchesRegex
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.string.NonEmptyString
import eu.timepit.refined.pureconfig._
import org.alexeyn.refined._

object refined {
  type ConnectionTimeout = Int Refined Interval.OpenClosed[W.`0`.T, W.`100000`.T]
  type MaxPoolSize = Int Refined Interval.OpenClosed[W.`0`.T, W.`100`.T]
  type JdbcUrl = String Refined MatchesRegex[W.`"""jdbc:\\w+://\\w+:[0-9]{4,5}/\\w+"""`.T]
}

final case class JdbcConfig(
  host: NonEmptyString,
  port: UserPortNumber,
  dbName: NonEmptyString,
  url: JdbcUrl,
  driver: NonEmptyString,
  user: NonEmptyString,
  password: NonEmptyString,
  connectionTimeout: ConnectionTimeout,
  maximumPoolSize: MaxPoolSize
)

final case class Server(host: NonEmptyString = "localhost", port: UserPortNumber = 8080)

object AppConfig extends StrictLogging {
  private val parseOptions = ConfigParseOptions.defaults().setAllowMissing(false)
  private val renderOptions = ConfigRenderOptions.defaults().setOriginComments(false)

  private val path = sys.env.getOrElse("APP_CONFIG_PATH", "src/main/resources/application.conf")

  implicit def hint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

  def load: Either[ConfigReaderFailures, (Server, Config)] = {
    val config = ConfigFactory.parseFile(new File(path), parseOptions).resolve()
    logger.debug("config content:\n {}", config.root().render(renderOptions))

    for {
      // validate storage config also
      _ <- loadConfig[JdbcConfig](config, "storage")
      c <- loadConfig[Server](config, "server")
    } yield c -> config
  }
}
