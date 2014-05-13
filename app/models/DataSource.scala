package models

import play.api.templates.Html
import play.api.libs.json.{JsValue, JsString, Writes, Json}
import scala.concurrent.{ExecutionContext, Future}
import util.AsyncCache
import controllers.Application

abstract class DataSource[O](val name: String) {
  def get(place: GooglePlace)(implicit context: ExecutionContext): Future[O] = {
//    AsyncCache.getOrElseAsync(s"$name:${place.placeRef}", Application.CacheExpr){
      retrieve(place)
//    }
  }

  protected def retrieve(place: GooglePlace)(implicit context: ExecutionContext): Future[O]
}

abstract class SubInfoFormatter[I](val slug: String, val name: String, val description: String, val dataSource: DataSource[I]) {
  def format(place: GooglePlace, data: I): SubInfoData
}

//trait SubInfoInput {
//  protected def loadFrom[O](dataSource: DataSource[O]): O
//}
//object DS1 extends DataSource[String]("ds1")
//object DS2 extends DataSource[GooglePlace]("ds2")
//
//class MyMultiSubInfoInput extends SubInfoInput {
//  def inputOne = loadFrom(DS1)
//  def inputTwo = loadFrom(DS2)
//}

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