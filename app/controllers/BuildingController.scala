package controllers

import _root_.util.AsyncCache
import play.api.mvc._
import play.api.libs.ws._
import scala.concurrent.{Promise, Future, ExecutionContext}
import scala.util.Random
import play.api.libs.json._
import models._
import play.api.cache.Cache
import models.GooglePlace
import play.api.libs.json.JsArray
import scala.Some

object BuildingController extends Controller {

  def infoPage(placeRef: String) = Action.async {
    implicit val context = scala.concurrent.ExecutionContext.Implicits.global

    googlePlaceDetails(placeRef).map(place => Ok(views.html.info(place, DataSources.all)))
  }

  private def googlePlaceDetails(placeRef: String)(implicit context: ExecutionContext) = {
    // TODO: any error handling whatsoever.  see https://developers.google.com/places/documentation/details

//    AsyncCache.getOrElseAsync(s"googleplace:$placeRef", Application.CacheExpr){

      WS.url("https://maps.googleapis.com/maps/api/place/details/json")
        .withQueryString(
          "sensor" -> "true",
          "reference" -> placeRef,
          "key" -> Application.googleApiKey)
        .get()
        .map { placeDetailsRaw =>
          val placeDetails = placeDetailsRaw.json \ "result"

          val address = (placeDetails \ "address_components").as[JsArray].value.map{component =>
            val key = (component \ "types")(0).as[String]
            val data = (component \ "long_name").as[String]
            key -> data
          }.toMap

          GooglePlace(
            (placeDetails \ "name").as[String],
            placeRef,
            (placeDetails \ "url").as[String],
            (placeDetails \ "geometry" \ "location" \ "lat").as[Double],
            (placeDetails \ "geometry" \ "location" \ "lng").as[Double],
            address
          )
        }
//    }
  }

  def subInfo(placeRef: String, sourceSlug: String) = Action.async {
    implicit val context = scala.concurrent.ExecutionContext.Implicits.global

    DataSources.allBySlug.get(sourceSlug) match {
      case None => Future.successful(BadRequest(s"Data source $sourceSlug doesn't exist."))
      case Some(subInfo) =>

        googlePlaceDetails(placeRef).flatMap{ place =>
          val result = processSubInfo(place, subInfo)
          result.map(r => Ok(Json.toJson(r))) // TODO: error checking
        }
    }
  }

  private def processSubInfo[I](place: GooglePlace, subInfo: SubInfoFormatter[I])(implicit context: ExecutionContext) = {
    subInfo.dataSource.get(place).map(data => subInfo.format(place, data))
  }
}

