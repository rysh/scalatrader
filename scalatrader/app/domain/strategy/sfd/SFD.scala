package domain.strategy.sfd

import domain.models.Ticker

class SFD(btc: Ticker, btcFx: Ticker, targetRange: BigDecimal = SFD.targetRange, unstableRange: BigDecimal = SFD.unstableRange) {
  import SFD._

  val deviationRate: BigDecimal = {
    if (btcFx.ltp > btc.ltp) {
      (BigDecimal(btcFx.ltp) / btc.ltp - 1) * 100
    } else {
      (BigDecimal(btc.ltp) / btcFx.ltp - 1) * 100 * -1
    }
  }

  def aroundSFD = buyZone || sellZone

  def buyZone: Boolean = borders.exists(b => deviationRate < b && (b - targetRange) <= deviationRate)

  def sellZone: Boolean = borders.exists(b => deviationRate > b && (b + targetRange) >= deviationRate)

  def tooClose: Boolean =
    borders.exists(b => {
      (deviationRate <= b && deviationRate >= (b - unstableRange)) ||
      (deviationRate >= b && deviationRate <= (b + unstableRange))
    })
}

object SFD {
  //SFD の算出に用いる比率：
  //① 10% 以上 15% 未満：0.5%
  //② 15% 以上 20% 未満：1.0%
  //③ 20% 以上 3.0%
  //マイナスの乖離率について公式の発表がないため確認待ち
  val borders: Seq[BigDecimal] = Seq[BigDecimal](10, 15, 20)
  val targetRange: BigDecimal = 0.5
  val unstableRange: BigDecimal = 0.09
}
