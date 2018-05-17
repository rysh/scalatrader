package controllers

import java.time.{ZonedDateTime}
import java.util
import javax.inject._

import application.BackTestApplication
import com.google.gson.Gson
import domain.backtest.BackTestResults
import domain.time.DateUtil
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import scala.collection.JavaConverters
import scala.concurrent.Future

@Singleton
class BackTestController @Inject()(cc: ControllerComponents, app: BackTestApplication) extends AbstractController(cc) with Secured {

  def main() = withAuth { _ => implicit request: Request[AnyContent] =>
    Ok(views.html.backtest()).withHeaders("Access-Control-Allow-Origin" -> " *")
  }

  val form = Form(
    mapping(
      "start" -> text,
      "end" -> text
    )(BackTestProps.apply)(BackTestProps.unapply)
  )

  var start: ZonedDateTime = DateUtil.now()
  var end: ZonedDateTime = DateUtil.now()

  case class ChartResponse(bars: util.List[ChartBar], values: util.List[TimedValue])
  case class ChartBar(key: Long, timestamp: String, high: Double, low: Double, open: Double, close: Double, label: String)
  case class TimedValue(timestamp: String, value: Double)

  def chart(): EssentialAction = withAuth { _ => implicit request: Request[AnyContent] =>
    import DateUtil._
    val orders = BackTestResults.valuesForChart(app.executors)
    val orderMap: Map[Long, (String, String)] = orders.map(a => (keyOf(ZonedDateTime.parse(a._3.timestamp)), (a._1, a._3.side))).toMap
    val bars = BackTestResults.candles1min.values
      .map(b => {
        ChartBar(b.key, keyToTimestamp(b.key), b.high, b.low, b.open, b.close, orderMap.get(b.key).map(label).getOrElse(""))
      })
      .toSeq
      .sortBy(_.key)

    val values1 = orders.toSeq.map(a => TimedValue(a._3.timestamp, a._2))

    val firstData = keyToTimestamp(bars.head.key)
    val lastDate = keyToTimestamp(bars.last.key)
    val lastValue = values1.lastOption.map(_.value).getOrElse(0.0)
    val values = TimedValue(firstData, 0) +: values1 :+ TimedValue(lastDate, lastValue)

    val gson: Gson = new Gson()
    val json = gson.toJsonTree(ChartResponse(JavaConverters.seqAsJavaList(bars), JavaConverters.seqAsJavaList(values))).toString
    Ok(Json.parse(json)).withHeaders("Access-Control-Allow-Credentials" -> "true")
  }

  private def label(a: (String, String)): String = {
    val (inOut, side) = a
    (if (inOut == "entry") "E" else "C") + side.substring(0, 1)
  }

  def ticker(): EssentialAction = withAuth { _ => implicit request: Request[AnyContent] =>
    val props: BackTestProps = form.bindFromRequest().get
    val start = DateUtil.of(props.start)
    val end = DateUtil.of(props.end)
    val tickers = JavaConverters.seqAsJavaList(
      BackTestResults.tickers
        .filter(t => {
          val ticketTime = ZonedDateTime.parse(t.timestamp)
          ticketTime.isAfter(start) && ticketTime.isBefore(end)
        })
        .map(t => (t.timestamp, t.ltp))
        .toSeq)
    val gson: Gson = new Gson()
    val json = gson.toJsonTree(tickers).toString
    Ok(Json.parse(json)).withHeaders("Access-Control-Allow-Credentials" -> "true")
  }

  def momentum(): EssentialAction = withAuth { _ => implicit request: Request[AnyContent] =>
    val props: BackTestProps = form.bindFromRequest().get
    val start = DateUtil.of(props.start)
    val end = DateUtil.of(props.end)
    val moments = JavaConverters.seqAsJavaList(
      BackTestResults.momentum
        .map(t => (DateUtil.parseKey(t._1), t._2))
        .filter(t => t._1.isAfter(start) && t._1.isBefore(end))
        .map(t => TimedValue(t._1.toOffsetDateTime.toString, t._2))
        .toSeq)
    val gson: Gson = new Gson()
    val json = gson.toJsonTree(moments).toString
    Ok(Json.parse(json)).withHeaders("Access-Control-Allow-Credentials" -> "true")
  }

  case class BackTestProps(start: String, end: String)

  def run(): EssentialAction = withAuth { _ => implicit request: Request[AnyContent] =>
    val props: BackTestProps = form.bindFromRequest().get
    start = DateUtil.of(props.start)
    end = DateUtil.of(props.end)
    Future {
      try {
        app.run(start, end)
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }(scala.concurrent.ExecutionContext.Implicits.global)
    val gson: Gson = new Gson()
    Ok(Json.parse(gson.toJsonTree(props).toString)).withHeaders("Access-Control-Allow-Credentials" -> "true")
  }
}
