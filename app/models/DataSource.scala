package models

import play.api.templates.Html
import play.api.libs.json.{JsValue, JsString, Writes, Json}
import scala.concurrent.{ExecutionContext, Future}

abstract class DataSource(val slug: String, val name: String, val description: String) {
  def getData(placeRef: String)(implicit context: ExecutionContext): Future[SubInfoData]
}

case class SubInfoData(result: SubInfoResult, html: Html)
object SubInfoData {
  implicit val writes = new Writes[SubInfoData] {
    override def writes(o: SubInfoData): JsValue = Json.obj(
      "result" -> o.result.cssClass,
      "html" -> o.html.toString()
    )
  }
}

sealed abstract class SubInfoResult(val cssClass: String)
object SubInfoResult {
  case object Positive extends SubInfoResult("positive")
  case object Neutral extends SubInfoResult("neutral")
  case object Negative extends SubInfoResult("negative")
  case object Unknown extends SubInfoResult("unknown")
}