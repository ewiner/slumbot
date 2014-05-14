package models

import scala.concurrent.ExecutionContext
import play.api.libs.ws.WS
import controllers.Application
import play.api.libs.json.JsArray

case class GooglePlace(name: String, placeRef: String, url: String, latitude: Double, longitude: Double, address: Map[String, String])

object GooglePlace {

  def get(placeRef: String)(implicit context: ExecutionContext) = {
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

}