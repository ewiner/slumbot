package models

import scala.concurrent.ExecutionContext
import play.api.libs.ws.WS
import play.api.libs.json.{JsValue, JsArray}
import org.joda.time.DateTime
import models.SubInfoResult.Neutral
import scala.util.Try
import org.joda.time.format.ISODateTimeFormat

case class ThreeOneOneCall(distanceFt: Double, createdDate: DateTime, status: String, agency: String, complaintType: String, descriptor: String)

object ThreeOneOneDataSource extends DataSource[Seq[ThreeOneOneCall]]("311") {
  val LatitudeBounds = 0.0014
  val LongitudeBounds = 0.0025

  val InvalidAgencies = Set("TLC", "DCA", "DOT")

  protected def retrieve(place: GooglePlace)(implicit context: ExecutionContext) = {
    query311table(place).map{data =>

      val allRows = data.json.as[JsArray].value
      def notTaxiOrBusiness(row: JsValue) = !InvalidAgencies.contains((row \ "agency").as[String])
      
      for (row <- allRows.filter(notTaxiOrBusiness)) yield {
        def jsStr(key: String) = (row \ key).as[String]

        // numbers come back from the SODA2 API as strings, for more precision
        val latitude = jsStr("latitude").toDouble
        val longitude = jsStr("longitude").toDouble
        val distance = haversine(place.latitude, place.longitude, latitude, longitude) * 3280.8 // km to feet

        val createdDate = ISODateTimeFormat.dateTimeParser.parseDateTime(jsStr("created_date"))

        ThreeOneOneCall(distance, createdDate, jsStr("status"), jsStr("agency"), jsStr("complaint_type"), jsStr("descriptor"))
      }
    }
  }

  private def query311table(place: GooglePlace) = {
    val minLat = place.latitude - LatitudeBounds
    val maxLat = place.latitude + LatitudeBounds
    val minLng = place.longitude - LongitudeBounds
    val maxLng = place.longitude + LongitudeBounds

    WS.url("http://data.cityofnewyork.us/resource/erm2-nwe9.json")
      .withQueryString("$where" -> s"within_box(location,$maxLat,$minLng,$minLat,$maxLng)",
                       "$order" -> "created_date DESC")
      .withRequestTimeout(60000)
      .get()
  }

  /**
   * Calculates the distance in km between two lat/long points
   * using the haversine formula
   *
   * From http://stackoverflow.com/a/18862550/17697
   */
  def haversine(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double = {
    val r = 6378.137
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lng2 - lng1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    val d = r * c
    d
  }
}

object NoiseComplaintSubInfo extends SubInfoFormatter[Seq[ThreeOneOneCall]]("noise", "Noise Complaints", "noise complaints", ThreeOneOneDataSource) {
  def format(place: GooglePlace, data: Seq[ThreeOneOneCall]) = {
    val noiseComplaints = data.filter(_.complaintType.toLowerCase.contains("noise"))
    val sorted = noiseComplaints.sortBy(_.distanceFt)
    SubInfoData(Neutral, views.html.noise(sorted))
  }
}