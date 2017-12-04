package application

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import adapter.aws.S3
import com.amazonaws.regions.Regions
import com.google.gson.Gson
import domain.models.Ticker
import domain.time.DateUtil.format
import domain.time.{DateUtil, MockedTime}
import org.scalatest.FunSuite

class BackTestRunnerTest extends FunSuite {

  test("testStart") {
  }
}
