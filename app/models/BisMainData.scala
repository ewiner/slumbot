package models

import scala.concurrent.{Future, ExecutionContext}
import org.jsoup.Jsoup
import org.jsoup.nodes.Entities.EscapeMode
import org.jsoup.nodes.Document
import scala.util.Try
import scala.collection.JavaConverters._
import play.api.templates.Html

case class BisMainData(bin: Int, block: Int, lot: Int, classification: String, stats: Map[String, (Int, Int)])

object DobComplaintsSubInfo extends SubInfoFormatter("dobcomplaints", "Building Complaints", BisMainDataSource) {
  def format(place: GooglePlace, data: BisMainData) = {
    val (total, open) = data.stats("Complaints")
    val openBlurb = if (open == 0) "" else s", including <strong>$open</strong> complaints still outstanding,"
    val blurbBase = s"There have been <strong>$total</strong> complaints$openBlurb made to the Department of Buildings."

    val (result, blurb) = if (data.classification.startsWith("C7")) {
      val (result, blurbPart) = total match {
        case c if c < 5 => (SubInfoResult.Positive, "below average")
        case c if c > 20 => (SubInfoResult.Negative, "well above average")
        case c if c > 9 => (SubInfoResult.Negative, "above average")
        case _ => (SubInfoResult.Neutral, "about average")
      }
      val judgeBlurb = blurbBase.dropRight(1) + s", which is <strong>$blurbPart</strong> for walk-up apartments."
      (result, judgeBlurb)
    } else {
      val result = if (open > 0) SubInfoResult.Negative else SubInfoResult.Unknown
      (result, blurbBase)
    }

    SubInfoData(result, Html(blurb), views.html.dobcomplaintdetail(data))
  }
}

object DobViolationsSubInfo extends SubInfoFormatter("dobviolations", "Building Code Violations", BisMainDataSource) {
  def format(place: GooglePlace, data: BisMainData) = {
    val (totalDob, openDob) = data.stats("Violations-DOB")
    val (totalEcb, openEcb) = data.stats("Violations-ECB (DOB)")
    val total = totalDob + totalEcb
    val open = openDob + openEcb

    val openBlurb = if (open == 0) "" else s", including <strong>$open</strong> violations still outstanding"
    val blurbBase = s"There have been <strong>$total</strong> building code violations$openBlurb."

    val (result, blurb) = if (data.classification.startsWith("C7")) {
      val (result, blurbPart) = total match {
        case c if c < 4 => (SubInfoResult.Positive, "below average")
        case c if c > 15 => (SubInfoResult.Negative, "well above average")
        case c if c > 8 => (SubInfoResult.Negative, "above average")
        case _ => (SubInfoResult.Neutral, "about average")
      }
      val judgeBlurb = blurbBase.dropRight(1) + s", which is <strong>$blurbPart</strong> for walk-up apartments."
      (result, judgeBlurb)
    } else {
      val result = if (open > 0) SubInfoResult.Negative else SubInfoResult.Unknown
      (result, blurbBase)
    }

    SubInfoData(result, Html(blurb), views.html.dobviolationdetail(data, totalDob, totalEcb))
  }
}

object BisMainDataSource extends DataSource[BisMainData]("bismain") {

  val us = """[\p{Z}\s]"""  // unicode whitespace, include &nbsp;
  val BinMatcher = s"BIN#$us+(\\d+)$us*".r
  val BlockLotMatcher = s":$us+(\\d+)$us*".r

  protected def retrieve(place: GooglePlace)(implicit context: ExecutionContext) = {
    Future{
      val boroId = place.address("administrative_area_level_2") match {
        case "New York County" => 1
        case "Bronx County" => 2
        case "Kings County" => 3
        case "Queens County" => 4
        case "Richmond County" => 5
      }

      Jsoup
        .connect("http://a810-bisweb.nyc.gov/bisweb/PropertyProfileOverviewServlet")
        .data(Map(
        "boro" -> boroId.toString,
        "houseno" -> place.address("street_number"),
        "street" -> place.address("route"),
        "go2" -> " GO ",
        "requestid" -> "0",
        "t10" -> "y").asJava)
        .timeout(10000)
        .get()

    }.map{doc =>

      doc.outputSettings().escapeMode(EscapeMode.xhtml)

      // TODO: error handling, once again

      val BinMatcher(binTxt) = doc.select("td.maininfo[align=right]").text()
      val bin = binTxt.toInt

      val BlockLotMatcher(blockTxt) = nextTdAfter("Tax Block", doc)
      val block = blockTxt.toInt

      val BlockLotMatcher(lotTxt) = nextTdAfter("Tax Lot", doc)
      val lot = lotTxt.toInt

      val classification = nextTdAfter("Department of Finance Building Classification:", doc)

      val stats = parseStats(doc)

      BisMainData(bin, block, lot, classification, stats.toMap)
    }
  }

  private def nextTdAfter(label: String, doc: Document) = {
    doc.select(s"td.content:has(b:containsOwn($label)) + td.content").text
  }

  private def parseStats(doc: Document) = {
    val statsTable = doc.select("td[width=390] > table > tbody > tr") // uggghh
    val statsRows = statsTable.asScala.drop(1).dropRight(1) // drop the first and last rows
                      .map(_.select("td"))
                      .filter(_.size() > 1) // only choose rows with more than one TD (sometimes BIS puts a message
                                            // across all three columns in this table)

    val stats = for (statsRow <- statsRows) yield {
        val cells = statsRow.select("td").asScala
        val label = cells(0).text
        val total = cells(1).text.toInt
        val open = Try(cells(2).text.toInt).getOrElse(0)
        label ->(total, open)
      }
    stats
  }
}
