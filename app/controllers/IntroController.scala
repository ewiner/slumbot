package controllers

import play.api._
import play.api.mvc._

object IntroController extends Controller {

  def index() = Action {
    Ok(views.html.index())
  }
}
