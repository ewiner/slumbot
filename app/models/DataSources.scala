package models

import scala.util.{Try, Random}
import play.api.templates.Html
import scala.concurrent.{ExecutionContext, Future}
import org.jsoup.Jsoup
import scala.collection.JavaConverters._
import org.jsoup.nodes.Entities.EscapeMode
import org.jsoup.nodes.Document
import scala.collection.mutable

object DataSources {

  val all = Seq(
    new RandomSubInfo("311", "311 Complaints"),
    new RandomSubInfo("boiler", "Boiler Maintenance"),
    BisSubInfo,
    NoiseComplaintSubInfo
  )

  lazy val allBySlug: Map[String, SubInfoFormatter[_]] = Map(all.map(ds => ds.slug -> ds) :_*)

  object NoDataSource extends DataSource[Null]("nodata") {
    protected def retrieve(place: GooglePlace)(implicit context: ExecutionContext) = Future.successful(null)
  }

  class RandomSubInfo(slug: String, name: String) extends SubInfoFormatter(slug, name, "Random data", NoDataSource) {

    def format(place: GooglePlace, data: Null) = {

      val result = Random.nextInt(4) match {
        case 0 => SubInfoResult.Positive
        case 1 => SubInfoResult.Neutral
        case 2 => SubInfoResult.Negative
        case 3 => SubInfoResult.Unknown
      }

      val html = Html("test <b>test</b> test!")
      SubInfoData(result, html)
    }

  }


}
