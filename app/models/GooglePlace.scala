package models

case class GooglePlace(name: String, placeRef: String, url: String, latitude: Double, longitude: Double, address: Map[String, String])