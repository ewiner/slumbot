package models

import play.api.templates.Html
import play.api.libs.json.JsValue
import scala.util.Random

abstract class SubInfoFormatter[I](val slug: String, val name: String, val dataSource: DataSource[I]) {
  def format(place: GooglePlace, data: I): SubInfoData
}

object SubInfoFormatter {

  val all = Seq[SubInfoFormatter[_]](
    DobViolationsSubInfo,
    DobComplaintsSubInfo,
    NoiseComplaintsSubInfo
  )

  lazy val allBySlug = Map(all.map(ds => ds.slug -> ds) :_*)

  def random(slug: String, name: String): SubInfoFormatter[Null] = new SubInfoFormatter(slug, name, DataSource.NoDataSource) {

    def format(place: GooglePlace, data: Null) = {

      val result = Random.nextInt(4) match {
        case 0 => SubInfoResult.Positive
        case 1 => SubInfoResult.Neutral
        case 2 => SubInfoResult.Negative
        case 3 => SubInfoResult.Unknown
      }

      val html = Html("test <b>test</b> test!")
      SubInfoData(result, html, html)
    }

  }
}
