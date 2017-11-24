package domain.strategy.turtle

import java.util

import domain.models.Position

import scala.collection.mutable

object TurtleCore {

  val candles1min = new mutable.HashMap[Long, Bar]()
  var bar_10min: Option[Bar] = None
  var bar_20min: Option[Bar] = None
  var position: Option[Position] = None


}
