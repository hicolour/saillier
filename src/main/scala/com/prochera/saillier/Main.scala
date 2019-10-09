package com.prochera.saillier

import java.io.File

import cats.effect.{ContextShift, IO, Timer}
import com.prochera.saillier.config.config.Config
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {

  val cfg = Config.load()

  val baseConfig    = ConfigFactory.load()
  val config        = ConfigFactory.parseFile(new File("/opt/app/conf/application.conf")).withFallback(baseConfig)

}
