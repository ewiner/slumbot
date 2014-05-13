package models

import scala.util.Random
import play.api.templates.Html
import scala.concurrent.{ExecutionContext, Future}

object DataSources {

  val all = Seq(
    new RandomDataSource("311", "311 Complaints"),
    new RandomDataSource("boiler", "Boiler Maintenance")
  )

  lazy val allBySlug = Map(all.map(ds => ds.slug -> ds) :_*)

  class RandomDataSource(slug: String, name: String) extends DataSource(slug, name, "Random data source") {
    override def getData(placeRef: String)(implicit context: ExecutionContext) = {

      val result = Random.nextInt(4) match {
        case 0 => SubInfoResult.Positive
        case 1 => SubInfoResult.Neutral
        case 2 => SubInfoResult.Negative
        case 3 => SubInfoResult.Unknown
      }

      val html = Html("hello <b>hello</b> hello!")
      Future.successful(SubInfoData(result, html))
    }
  }

}
