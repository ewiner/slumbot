package models

import scala.concurrent.{Future, ExecutionContext}
import org.jsoup.Jsoup
import org.jsoup.nodes.Entities.EscapeMode
import org.jsoup.nodes.Document
import scala.util.Try
import scala.collection.JavaConverters._

case class BisMainData(bin: Int, block: Int, lot: Int, classification: String, stats: Map[String, (Int, Int)])

object BisSubInfo extends SubInfoFormatter("bisinfo", "General Building Info", "Just some stuff", BisMainDataSource) {
  def format(place: GooglePlace, data: BisMainData) = {
    SubInfoData(SubInfoResult.Neutral, views.html.bisinfo(data), views.html.bisinfo(data))
  }
}

object BisMainDataSource extends DataSource[BisMainData]("bismain") {

  val us = """[\p{Z}\s]"""  // unicode whitespace, include &nbsp;
  val BinMatcher = s"BIN#$us+(\\d+)$us*".r
  val BlockLotMatcher = s":$us+(\\d+)$us*".r

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
    val stats = for (statsRow <- statsRows) yield {
        val cells = statsRow.children.asScala
        val label = cells(0).text
        val total = cells(1).text.toInt
        val open = Try(cells(3).text.toInt).getOrElse(0)
        label ->(total, open)
      }
    stats
  }
}
