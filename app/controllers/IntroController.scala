package controllers

import play.api._
import play.api.mvc._

object IntroController extends Controller {

  def index() = Action { request =>
    // for now, you have to access the homepage with ?preview in the URL
    if (request.queryString.keySet.contains("preview")) {
      Ok(views.html.index())
    } else {
      Forbidden("Sorry, slumbot is not ready for prime-time yet.  Stay tuned, we'll be launching soon!")
    }
  }
}
