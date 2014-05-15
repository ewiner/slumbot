package controllers

import play.api.mvc._
import scala.concurrent.{Future, ExecutionContext}
import play.api.libs.json._
import models._
import scala.Some

object BuildingController extends Controller {

  def infoPage(placeRef: String) = Action.async {
    implicit val context = scala.concurrent.ExecutionContext.Implicits.global

    GooglePlace.get(placeRef).map(place => Ok(views.html.info(place, SubInfoFormatter.all)))
  }

  def subInfo(placeRef: String, sourceSlug: String) = Action.async {
    implicit val context = scala.concurrent.ExecutionContext.Implicits.global

    SubInfoFormatter.allBySlug.get(sourceSlug) match {
      case None => Future.successful(BadRequest(s"Data source $sourceSlug doesn't exist."))
      case Some(subInfo) =>

        for {
          place <- GooglePlace.get(placeRef)
          result <- processSubInfo(place, subInfo)
        } yield {
          Ok(Json.toJson(result)) // TODO: error checking
        }
    }
  }

  private def processSubInfo[I](place: GooglePlace, subInfo: SubInfoFormatter[I])(implicit context: ExecutionContext) = {
    subInfo.dataSource.get(place).map(data => subInfo.format(place, data))
  }
}

