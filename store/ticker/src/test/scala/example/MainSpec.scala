package example

import org.scalatest._

class MainSpec extends FlatSpec with Matchers {
  "The Hello object" should "say hello" in {
    Main.greeting shouldEqual "hello"
  }
}
