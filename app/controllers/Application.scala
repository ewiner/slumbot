package controllers

import play.api._
import play.api.mvc._
import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContext, Future}
import play.api.cache.Cache
import scala.util.Failure


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

  /**
   * A simple way to cache async computations.  Concurrent requests will all wait on the first request, even if its a
   * failure.  If it succeeds, it stays in the cache. If that first request is a failure, the next request will recompute.
   * <br><br>
   * In this example, only requests 1 and 4 will get kicked off.  Requests 1 through 3 will return failure, and 4 through 8
   * will return Success.
   *
   * <pre>
   * time ->
   *
   * 1 -------Failure
   *    2     Failure
   *       3  Failure
   *            4 -------Success
   *                5    Success
   *                  6  Success
   *                       7 Success
   *                           8 Success
   * </pre>
   */
  def cachedQuery[A](cacheKey: String)(operation: => Future[A])(implicit executionContext: ExecutionContext) = {
    import play.api.Play.current
    Cache.getAs[Future[A]](cacheKey).getOrElse{
      val op = operation
      Cache.set(cacheKey, op, CacheExpr)
      op.andThen{case Failure(_) => Cache.remove(cacheKey)}
    }
  }

  /* Why a complicated JavaScript redirection instead of a link?  Because this particular page on the BIS website checks
   * its referer header for "nyc.gov", or else it forwards you back to the BIS home page.  Since the routing URL for this
   * action has "nyc.gov" in it, it passes that check and shows you the right page.
   */
  def redirToComplaintsPage(bin: Int) = Action {
    Ok(views.html.dobcomplaintredir(bin))
  }
}
