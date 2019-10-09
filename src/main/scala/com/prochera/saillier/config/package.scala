package com.prochera.saillier.config

import cats.effect.IO
import com.typesafe.config.ConfigFactory
import pureconfig.error.ConfigReaderException

package object config {

  object Config {

    case class Config()


    import pureconfig._
    import pureconfig.generic.auto._

    def load(configFile: String = "application.conf"): IO[Config] = {
      IO {
        loadConfig[Config](ConfigFactory.load(configFile))
      }.flatMap {
        case Left(e) => IO.raiseError[Config](new ConfigReaderException[Config](e))
        case Right(config) => IO.pure(config)
      }
    }
  }
}