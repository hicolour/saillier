package io.findify.fops

import java.time.Instant
import java.time.format.DateTimeFormatter

import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, DecodingFailure, Encoder, Error }
import sttp.client.circe.asJson
import sttp.client.{ HttpURLConnectionBackend, Identity, NothingT, SttpBackend, basicRequest, _ }
import sttp.client._
import sttp.client.circe._
import scala.util.{ Failure, Success, Try }

object MetricsReporter {

  def MERCHANT_FEE_QUERY(apiKeyId: Int) =
    s"sum(admin_merchant_fee_in_month%7BapiKeyId%20%3D%27${apiKeyId}%27%2CpastMonth%3D%221%22%7D%20OR%20on()%20vector(0))"

  def MERCHANT_COST_QUERY(apiKeyId: Int, name: String) =
    s"(sum(clickhouse_merchant_event_count_for_month%7BapiKeyId%3D%27${apiKeyId}%27%2CpastMonth%3D%221%22%7D))%2F100000*(0.33%2B0.34%2B0.35)%20%2B%20(sum(kube_deployment_spec_replicas%7Bnamespace%3D%22search-prod%22%7D%20and%20on%20(deployment)%20kube_deployment_labels%7Blabel_app%3D%22lucy%22%2Cnamespace%3D%22search-prod%22%2C%20label_host%3D%27${name}%27%7D))*3.6%20%20%2B%20(21%20%2B%208%20%2B8.6)%20%0A%2B%20(19.6*count(kube_deployment_labels%7Blabel_app%3D%22merlin-worker%22%2Clabel_host%3D%27${name}%27%7D%20OR%20on()%20vector(0)))"

  def MERCHANT_REVENUE_QUERY(apiKeyId: Int) =
    s"clickhouse_merchant_revenue_for_month%7BapiKeyId%3D%27${apiKeyId}%27%2C%20pastMonth%3D%221%22%2Ctype%3D%22revenue%22%7D"

  def MERCHANT_API_CALLS_TOTAL_QUERY(apiKeyId: Int) =
    s"sum(clickhouse_merchant_event_count_for_month%7BapiKeyId%3D%27${apiKeyId}%27%2CpastMonth%3D%221%22%7D)"

  def MERCHANT_VISITS_TOTAL_QUERY(apiKeyId: Int) =
    s"sum(clickhouse_merchant_event_count_for_month%7BapiKeyId%3D%27${apiKeyId}%27%2CpastMonth%3D%221%22%2Ctype%3D%22visits%22%7D)"

  def MERCHANT_VISITS_PREVIOUS_TOTAL_QUERY(apiKeyId: Int) =
    s"sum(clickhouse_merchant_event_count_for_month%7BapiKeyId%3D%27${apiKeyId}%27%2CpastMonth%3D%222%22%2Ctype%3D%22visits%22%7D)"

  def MERCHANT_GROSS_MARGIN_QUERY(apiKeyId: Int) =
    s"""100-(((sum(clickhouse_merchant_event_count_for_month%7BapiKeyId%3D%27${apiKeyId}%27%2CpastMonth%3D%221%22%7D))%2F100000*(0.33%20%2B%200.34%20%2B0.35)%20%2B%20(sum(kube_deployment_spec_replicas%7Bnamespace%3D%22search-prod%22%7D%20and%20on%20(deployment)%20kube_deployment_labels%7Blabel_app%3D%22lucy%22%2Cnamespace%3D%22search-prod%22%2C%20label_host%3D%27woven-official.myshopify.com%27%7D))*3.6%20%20%2B%20(21%20%2B%208)%20%0A%2B%20(19.6*count(kube_deployment_labels%7Blabel_app%3D%22merlin-worker%22%2Clabel_host%3D%27woven-official.myshopify.com%27%7D%20OR%20on()%20vector(0))))*100%20%2F%20%0A(sum(admin_merchant_fee_in_month%7BapiKeyId%20%3D%27${apiKeyId}%27%2CpastMonth%3D%221%22%7D%20OR%20on()%20vector(0))%20%2B%200.01)%0A)"""

  implicit val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()

  case class PrometheusResult(data: Data)
  case class Data(result: List[Metric])
  case class Metric(value: List[Any])
  implicit val anyDecoder: Decoder[Any] = Decoder.instance(c => {
    c.focus match {
      case Some(x) => Right(x)
      case None    => Left(DecodingFailure("Could not parse", List()))
    }
  })
  implicit val prometheusResultDecoder: Decoder[PrometheusResult] = deriveDecoder[PrometheusResult]
  implicit val dataDecoder: Decoder[Data] = deriveDecoder[Data]
  implicit val metricDecoder: Decoder[Metric] = deriveDecoder[Metric]

  def getMetric(query: String, session: Option[String]): Option[Double] = {
    val time = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    val queryUrl = s"https://metrics.findify.io/api/datasources/proxy/17/api/v1/query?query=${query}&time=${time}"

    println(session.get)
    val response = basicRequest
      .get(uri"${queryUrl}")
      .header("Cookie", session.getOrElse(""))
      .response(asJson[PrometheusResult])
      .send()

    (response.body match {
      case Right(result) => {
        Try(result.data.result(0).value(1).toString.replaceAll("\"", "").toDouble) match {
          case Failure(exception) => {
            println(s"error while processing result: ${result}\n${query}" + exception.getMessage + "\n\n"); Some(0.0)
          }
          case Success(value) => Some(value)
          case _              => Some(0.0)
        }
      }
      case Left(_) => Some(0.0)
    })

  }

  case class LoginCredentials(user: String, password: String)

  case class AuthLogin(message: String)

  implicit val loginCredentialsDecoder: Decoder[LoginCredentials] = deriveDecoder[LoginCredentials]
  implicit val authLoginCredentialsDecoder: Decoder[AuthLogin] = deriveDecoder[AuthLogin]
  implicit val loginCredentialsEncoder: Encoder[LoginCredentials] = deriveEncoder[LoginCredentials]

  def login(): Option[String] = {

    val login = LoginCredentials("marek@findify.io", "m!5G9H@X#Auu")
    val tokenResponse: Identity[Response[Either[ResponseError[Error], AuthLogin]]] =
      basicRequest
        .post(uri"https://metrics.findify.io/login")
        .body(login)
        .response(asJson[AuthLogin])
        .send()

    tokenResponse.headers.find(h => h.name == "Set-Cookie") match {
      case Some(h) => Some(h.value)
      case None    => None
    }
  }

}
