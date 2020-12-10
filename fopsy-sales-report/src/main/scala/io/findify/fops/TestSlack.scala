package io.findify.fops

import java.text.SimpleDateFormat
import java.time.{ Instant, LocalDateTime }
import java.time.format.DateTimeFormatter
import java.util.{ Locale, TimeZone }
import sttp.client._
import sttp.client.circe._

import scala.collection.mutable
import scala.collection.mutable.{ ArrayBuffer, ListBuffer }
import scala.language.postfixOps
import scala.sys.process._
import scala.util.Try
import PrometheusReporter._
import io.findify.fops.SlackReport.FopsyChannel
import SlackReport._
import io.findify.fops.SlackReport.{ publishReportToSlackChannel, uploadToSlackChannel, FopsyChannel }
import sttp.client.{ HttpURLConnectionBackend, Identity, NothingT, SttpBackend }

object TestSlack extends App {

//  uploadToSlackChannel(
//    FopsyChannel,
//    "<http://www.foo.com|This message *is* a link>"
//  )
  implicit val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()

  val t = Instant.now();
  val ft = DateTimeFormatter.ISO_INSTANT.format(t);

  println(ft)
//  val a =
//    MetricsReporter.getMetric(s"max_over_time(max(aws_cost_explorer_daily_cost)%5B24h%5D)&time=${ft}")
//  println(a)
}
