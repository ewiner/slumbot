package models

import scala.concurrent.{ExecutionContext, Future}
import controllers.Application

abstract class DataSource[O](val cacheKey: String) {
  def get(place: GooglePlace)(implicit context: ExecutionContext): Future[O] = {

    Application.cachedQuery(s"$cacheKey:${place.placeRef}"){
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