package models

import play.api.templates.Html
import play.api.libs.json.JsValue
import scala.util.Random

abstract class SubInfoFormatter[I](val slug: String, val name: String, val description: String, val dataSource: DataSource[I]) {
  def format(place: GooglePlace, data: I): SubInfoData
}

object SubInfoFormatter {

  val all = Seq[SubInfoFormatter[_]](
    random("311", "311 Complaints"),
    random("boiler", "Boiler Maintenance"),
    BisSubInfo,
    NoiseComplaintSubInfo
  )

  lazy val allBySlug = all.map(ds => ds.slug -> ds).toMap

  def random(slug: String, name: String): SubInfoFormatter[Null] = new SubInfoFormatter(slug, name, "Random data", DataSource.NoDataSource) {

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
