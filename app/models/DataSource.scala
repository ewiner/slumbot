package models

import play.api.templates.Html
import play.api.libs.json.{JsValue, JsString, Writes, Json}
import scala.concurrent.{ExecutionContext, Future}
import util.AsyncCache
import controllers.Application

abstract class DataSource[O](val cacheKey: String) {
  def get(place: GooglePlace)(implicit context: ExecutionContext): Future[O] = {
//    AsyncCache.getOrElseAsync(s"$cacheKey:${place.placeRef}", Application.CacheExpr){
      retrieve(place)
//    }
  }

  protected def retrieve(place: GooglePlace)(implicit context: ExecutionContext): Future[O]
}

object DataSource {
  object NoDataSource extends DataSource[Null]("nodata") {
    protected def retrieve(place: GooglePlace)(implicit context: ExecutionContext) = Future.successful(null)
  }
}