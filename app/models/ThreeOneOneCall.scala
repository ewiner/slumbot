package models

import scala.concurrent.{Future, ExecutionContext}
import play.api.libs.ws.WS
import play.api.libs.json.JsArray
import org.joda.time.{Days, DateTime}
import org.joda.time.format.ISODateTimeFormat
import akka.event.slf4j.SLF4JLogging
import scala.util.Try

case class ThreeOneOneCall(distanceFt: Double, createdDate: DateTime, location: String, status: String, agency: String, complaintType: String, descriptor: String)

object ThreeOneOneDataSource extends DataSource[Seq[ThreeOneOneCall]]("311") with SLF4JLogging {

  val LatitudeBounds = 0.0028 // about 2000 ft in both directions
  val LongitudeBounds = 0.0036

  protected def retrieve(place: GooglePlace)(implicit context: ExecutionContext) = {
    query311table(place).map{allRows =>

      for (row <- allRows) yield {
        def jsStr(key: String) = Try((row \ key).as[String]).getOrElse{
          println(s"failed: $key from $row")
          "Unknown"
        }

        // numbers come back from the SODA2 API as strings, for more precision
        val latitude = jsStr("latitude").toDouble
        val longitude = jsStr("longitude").toDouble
        val distance = haversine(place.latitude, place.longitude, latitude, longitude)

        val createdDate = ISODateTimeFormat.dateTimeParser.parseDateTime(jsStr("created_date"))

        val location = jsStr("address_type") match {
          case "ADDRESS" | "LATLONG" => jsStr("incident_address")
          case "INTERSECTION" => "%s & %s".format(jsStr("intersection_street_1"), jsStr("intersection_street_2"))
          case "BLOCKFACE" => "%s between %s & %s".format(jsStr("incident_address"), jsStr("cross_street_1"), jsStr("cross_street_2"))
          case "PLACENAME" => jsStr("landmark")
          case _ => "Unknown"
        }

        ThreeOneOneCall(distance, createdDate, location, jsStr("status"), jsStr("agency"), jsStr("complaint_type"), jsStr("descriptor"))
      }
    }
  }

  private def query311table(place: GooglePlace)(implicit context: ExecutionContext) = {
    val minLat = place.latitude - LatitudeBounds
    val maxLat = place.latitude + LatitudeBounds
    val minLng = place.longitude - LongitudeBounds
    val maxLng = place.longitude + LongitudeBounds

//    val calls = for (agency <- Seq("DEP", "NYPD")) yield {
      val timer = System.currentTimeMillis()
      WS.url("http://data.cityofnewyork.us/resource/iyqm-3c4n.json")
        .withQueryString("$where" -> s"within_box(location,$maxLat,$minLng,$minLat,$maxLng)")
        .withRequestTimeout(60000)
        .get()
        .andThen{case _ => log.debug(s"finished 311 calls in ${System.currentTimeMillis() - timer} ms")}
        .map(_.json.as[JsArray].value)
//    }
//
//    Future.reduce(calls)(_++_)
  }

  /**
   * Calculates the distance in feet between two lat/long points
   * using the haversine formula
   *
   * From http://stackoverflow.com/a/18862550/17697
   */
  def haversine(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double = {
    val r = 6378.137 // earth's radius in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lng2 - lng1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    val d = r * c
    d * 3280.8 // km to feet
  }
}

object NoiseComplaintsSubInfo extends SubInfoFormatter[Seq[ThreeOneOneCall]]("noise", "Noise Complaints", ThreeOneOneDataSource) {
  def isRecentNoiseComplaint(call: ThreeOneOneCall) = {
    Days.daysBetween(call.createdDate, DateTime.now).getDays <= 365
  }

  val AvgRatio = (300 * 300 * math.Pi) / (2000 * 2000)

  def format(place: GooglePlace, data: Seq[ThreeOneOneCall]) = {
    val noiseComplaints = data.filter(isRecentNoiseComplaint)

    val nearby = noiseComplaints.filter(call => call.distanceFt <= 300).toSeq

    val ratio = nearby.length / (noiseComplaints.length * AvgRatio)

    val (result, blurbPart) = ratio match {
      case _ if ratio > 2.0 => (SubInfoResult.Negative, "well above average")
      case _ if ratio < 0.4 => (SubInfoResult.Positive, "well below average")
      case _ if ratio < 0.8 => (SubInfoResult.Positive, "below average")
      case _ if ratio > 1.3 => (SubInfoResult.Negative, "above average")
      case _ => (SubInfoResult.Neutral, "about average")
    }

    SubInfoData(result, views.html.noiseblurb(nearby, blurbPart), views.html.noisedetail(nearby))
  }
}