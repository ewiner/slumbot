package models

import scala.util.Random
import play.api.templates.Html
import scala.concurrent.{ExecutionContext, Future}
import org.jsoup.Jsoup
import play.api.libs.ws.WS
import scala.collection.JavaConverters._

object DataSources {

  val all = Seq(
    new RandomSubInfo("311", "311 Complaints"),
    new RandomSubInfo("boiler", "Boiler Maintenance"),
    BisSubInfo
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

      val html = Html("hello <b>hello</b> hello!")
      SubInfoData(result, html)
    }

  }

  object BisMainDataSource extends DataSource[BisMainData]("bismain") {

    val BinMatcher = """BIN#[\p{Z}\s]+(\d+)[\p{Z}\s]*""".r
    val BlockLotMatcher = """:[\p{Z}\s]+(\d+)[\p{Z}\s]*""".r

    protected def retrieve(place: GooglePlace)(implicit context: ExecutionContext) = {
      Future{
        Jsoup
          .connect("http://a810-bisweb.nyc.gov/bisweb/PropertyProfileOverviewServlet")
          .data(Map(
            "boro" -> "1",
            "houseno" -> "208",
            "street" -> "E 6th St",
            "go2" -> " GO ",
            "requestid" -> "0",
            "t10" -> "y").asJava)
          .timeout(6000)
          .get()

      }.map{doc =>

        val BinMatcher(binTxt) = doc.select("td.maininfo[align=right]").text()
        val bin = binTxt.toInt
        
        val BlockLotMatcher(blockTxt) = doc.select("td.content:has(b:containsOwn(Tax Block)) + td.content").text
        val block = blockTxt.toInt

        val BlockLotMatcher(lotTxt) = doc.select("td.content:has(b:containsOwn(Tax Lot)) + td.content").text
        val lot = lotTxt.toInt

        val classification = doc.select("td.content:has(b:containsOwn(Department of Finance Building Classification:)) + td.content").text
        BisMainData(bin, block, lot, classification, Map())
      }
    }
  }

  case class BisMainData(bin: Int, block: Int, lot: Int, classification: String, stats: Map[String, (Int, Int)])
  
  object BisSubInfo extends SubInfoFormatter("bisinfo", "General Building Info", "Just some stuff", BisMainDataSource) {
     def format(place: GooglePlace, data: BisMainData) = {
      SubInfoData(SubInfoResult.Neutral, views.html.bisinfo(data))
    }
  }

}
