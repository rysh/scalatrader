package domain.strategy

import org.scalatest.FunSuite
import domain.Side.Buy
import domain.models.Ordering

class StrategyStateTest extends FunSuite {

  test("StrategyState json decode/encode") {
    val origin = StrategyState(0L, "MomentumReverse", false, 1.5, Some("xxx-xx-xx"), Some(Ordering(Buy, 0.2, true)), Map.empty)

    import io.circe.syntax._
    import io.circe.generic.auto._
    val json = origin.asJson.toString()

    import io.circe.parser._
    import io.circe.generic.auto._
    val parsed: StrategyState = decode[StrategyState](json) match {
      case Right(ex) => ex
      case Left(err) => throw err
    }
    assert(origin === parsed)
  }
}
