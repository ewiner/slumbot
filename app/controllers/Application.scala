package controllers

import play.api._
import play.api.mvc._


object Application extends Controller {

  def googleApiKey = Play.current.configuration.getString("application.googlekey").getOrElse(throw new IllegalStateException("Must specify a Google API key in application.conf"))

  def jsRoutes = Action { implicit request =>
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        routes.javascript.BuildingController.infoPage
      )
    ).as("text/javascript")
  }

}
