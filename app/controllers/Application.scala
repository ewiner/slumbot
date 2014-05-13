package controllers

import play.api._
import play.api.mvc._
import java.util.concurrent.TimeUnit


object Application extends Controller {

  val CacheExpr = TimeUnit.DAYS.toSeconds(1).toInt

  def googleApiKey = Play.current.configuration.getString("google.apikey").getOrElse(throw new IllegalStateException("Must specify a Google API key in application.conf"))
  def kimonoApiKey = Play.current.configuration.getString("kimono.apikey").getOrElse(throw new IllegalStateException("Must specify a Kimono API key in application.conf"))

  def jsRoutes = Action { implicit request =>
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        routes.javascript.BuildingController.infoPage,
        routes.javascript.BuildingController.subInfo
      )
    ).as("text/javascript")
  }

}
