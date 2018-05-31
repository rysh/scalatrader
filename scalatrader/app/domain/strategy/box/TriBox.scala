package domain.strategy.box

import domain.strategy.core.Box

case class TriBox(shortRange: Option[Box], middleRange: Option[Box], longRange: Option[Box]) {

  def isDefined: Boolean = shortRange.isDefined && middleRange.isDefined && longRange.isDefined

  def isUp: Boolean = shortRange.get.isUp && middleRange.get.isUp && longRange.get.isUp

  def isDown: Boolean = shortRange.get.isDown && middleRange.get.isDown && longRange.get.isDown
}
