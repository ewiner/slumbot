package models

import play.api.templates.Html
import play.api.libs.json.{Writes, JsValue, Json}

case class SubInfoData(result: SubInfoResult, blurb: Html, details: Html)

object SubInfoData {
  implicit val writes = new Writes[SubInfoData] {
    override def writes(o: SubInfoData): JsValue = Json.obj(
      "result" -> o.result.cssClass,
      "blurb" -> o.blurb.toString,
      "details" -> o.details.toString()
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
