package controllers

import play.api.mvc._
import play.api.libs.ws._
import scala.concurrent.{Promise, Future, ExecutionContext}
import scala.util.Random
import play.api.libs.json._
import models.DataSource

object BuildingController extends Controller {

  val DataSources = Seq(
    DataSource("311", "311 Complaints", "foo"),
    DataSource("boiler", "Boiler Maintenance", "foo")
  )

  def infoPage(placeRef: String) = Action.async {
    implicit val context = scala.concurrent.ExecutionContext.Implicits.global
    getAddressFromRef(placeRef).map(addr => Ok(views.html.info(placeRef, addr, DataSources)))
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

  val ResultTypes = Seq("positive", "neutral", "negative", "unknown")
  def subInfo(placeRef: String, sourceSlug: String) = Action.async {
    println(sourceSlug)

    implicit val context = scala.concurrent.ExecutionContext.Implicits.global

    val result = ResultTypes(Random.nextInt(4))
    val html = "hello hello hello!"
    val json = Json.obj(
      "result" -> result,
      "html" -> html
    )

    Future.successful(Ok(json))
  }
}
