package io.findify.fops

import akka.actor.ActorSystem
import io.circe.Error
import io.findify.fops.FOPSyReport.{ requestPayload, AuthToken }
import sttp.client.circe.asJson
import sttp.client.{ basicRequest, HttpURLConnectionBackend, Identity, NothingT, Response, ResponseError, SttpBackend }
import io.circe._
import io.circe.generic.semiauto._
import scalaj.http.{ Http, HttpOptions }
import sttp.client._
import sttp.client.circe._
object HttpClickHouseClient {
  val ip = "10.128.1.49"
  val port = 8123
  val queryDatabase: String = "prod"

  val searchTableName = "search"

  implicit val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()

  def getLastMonthsTotalCount(apiKeyId: Int): Either[ResponseError[Error], Double] = {
    val (lower, upper) = getTimeBound(1)
    val query = getTotalCountQuery("", apiKeyId, upper, lower)
    getSingleValue(query)
  }

  private def getSingleValue(query: String) = {
    val response: Identity[Response[Either[ResponseError[Error], Double]]] =
      basicRequest
        .post(uri"http://${ip}:$port")
        .body(query)
        .response(asJson[Double])
        .send()

    response.body
  }

  private def getCountTodayQuery(table: String, apiKeyId: String): String =
    getCountQueryToday(table, apiKeyId, "today()", "yesterday()")

  private def getCountPastMonthQuery(table: String, apiKeyId: String, monthsPast: Int): String = {
    val (lowerBound, upperBound) = getTimeBound(monthsPast)
    getCountQuery(table, apiKeyId, upperBound, lowerBound)
  }

  private def getTimeBound(monthsPast: Int): (String, String) =
    (
      s"toDateTime(toStartOfMonth(subtractMonths(now(),${monthsPast})))",
      s"toDateTime(toStartOfMonth(subtractMonths(now(),${monthsPast + 1})))"
    )

  private def getCountThisMonthQuery(table: String, apiKeyId: String): String =
    getCountQuery(table, apiKeyId, "today()", "yesterday()")

  private def getCountQueryToday(table: String, apiKeyId: String, upperBound: String, lowerBound: String): String =
    s"select count(*) from ${table} where apiKeyGroupId = ${apiKeyId} AND timeServer <= ${upperBound} AND timeServer > ${lowerBound}"

  private def getCountQuery(table: String, apiKeyId: String, upperBound: String, lowerBound: String): String =
    s"select count(*) from ${table} where apiKeyGroupId = ${apiKeyId} AND timeServer >= ${lowerBound} AND timeServer < ${upperBound}"

  private def getTotalCountQuery(table: String, apiKeyId: Int, upperBound: String, lowerBound: String): String =
    s"""SELECT sum(*)
       |  FROM(
       |        select count(*)
       |          from prod.search
       |         where apiKeyGroupId = $apiKeyId
       |           AND timeServer >= ${lowerBound} AND ${upperBound}
       |     UNION ALL select count(*)
       |          from prod.recommendation
       |         where apiKeyGroupId = $apiKeyId
       |           AND timeServer >= ${lowerBound} AND ${upperBound}
       |     UNION ALL select count(*)
       |          from prod.autocomplete
       |         where apiKeyGroupId = $apiKeyId
       |          AND timeServer >= ${lowerBound} AND ${upperBound}  
       |       )
       |""".stripMargin
//  AND timeServer >= toDateTime(toStartOfMonth(now())) aND  timeServer < toDateTime(toStartOfMonth(addMonths(now(), 1)))

}
