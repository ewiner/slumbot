package controllers

import play.api.mvc._
import play.api.libs.ws._
import scala.concurrent.ExecutionContext

object BuildingController extends Controller {

  def infoPage(placeRef: String) = Action.async {
    implicit val context = scala.concurrent.ExecutionContext.Implicits.global
    getAddressFromRef(placeRef).map(addr => Ok(views.html.info(addr)))
  }

  private def getAddressFromRef(placeRef: String)(implicit context: ExecutionContext) = {
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


}
