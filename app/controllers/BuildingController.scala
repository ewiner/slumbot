package controllers

import play.api.mvc._
import play.api.libs.ws._
import scala.concurrent.{Promise, Future, ExecutionContext}
import scala.util.Random
import play.api.libs.json._
import models.{SubInfoData, DataSources, DataSource}

object BuildingController extends Controller {

  def infoPage(placeRef: String) = Action.async {
    implicit val context = scala.concurrent.ExecutionContext.Implicits.global
    getAddressFromRef(placeRef).map(addr => Ok(views.html.info(placeRef, addr, DataSources.all)))
  }

  private def getAddressFromRef(placeRef: String)(implicit context: ExecutionContext) = {
    // TODO: any error handling whatsoever.  see https://developers.google.com/places/documentation/details

    WS.url("https://maps.googleapis.com/maps/api/place/details/json")
      .withQueryString(
        "sensor" -> "true",
        "reference" -> placeRef,
        "key" -> Application.googleApiKey)
      .get()
      .map { placeDetailsRaw =>
        val placeDetails = placeDetailsRaw.json
        (placeDetails \ "result" \ "name").as[String]
      }
  }

  def subInfo(placeRef: String, sourceSlug: String) = Action.async {
    implicit val context = scala.concurrent.ExecutionContext.Implicits.global

    DataSources.allBySlug.get(sourceSlug) match {
      case None => Future.successful(BadRequest(s"Data source $sourceSlug doesn't exist."))
      case Some(dataSource) =>
        val result = dataSource.getData(placeRef)
        result.map(r => Ok(Json.toJson(r))) // TODO: error checking
    }
  }
}
