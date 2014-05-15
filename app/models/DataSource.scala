package models

import scala.concurrent.{ExecutionContext, Future}
import controllers.Application
import play.api.cache.Cache

abstract class DataSource[O](val cacheKey: String) {
  def get(place: GooglePlace)(implicit context: ExecutionContext): Future[O] = {
    import play.api.Play.current
    Cache.getOrElse(s"$cacheKey:${place.placeRef}", Application.CacheExpr){
      retrieve(place)
    }
  }

  protected def retrieve(place: GooglePlace)(implicit context: ExecutionContext): Future[O]
}

object DataSource {
  object NoDataSource extends DataSource[Null]("nodata") {
    protected def retrieve(place: GooglePlace)(implicit context: ExecutionContext) = Future.successful(null)
  }
}