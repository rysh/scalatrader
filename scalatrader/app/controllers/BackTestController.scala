package controllers

import java.time.format.DateTimeFormatter.ofPattern
import java.time.{ZonedDateTime, LocalDateTime, ZoneId}
import java.util
import javax.inject._

import application.BackTestApplication
import application.settings.UserApplication
import com.google.gson.Gson
import domain.strategy.turtle.{Bar, BackTestResults}
import domain.time.DateUtil
import domain.user.Settings
import io.circe.{Encoder, Decoder}
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
      "start" -> text ,
      "end" -> text
    )(BackTestProps.apply)(BackTestProps.unapply)
  )

  case class ChartResponse(bars: util.List[ChartBar], values: util.List[(String, Int, BackTestResults.OrderResult, Int)])
  case class ChartBar(key: Long, high: Double, low: Double, open: Double, close: Double, label: String)

    def chart() = withAuth { _ => implicit request: Request[AnyContent] =>

    val orders = BackTestResults.valuesForChart()
    val orderMap: Map[Long, (String, String)] = orders.map(a => (DateUtil.keyOfUnit1Minutes(ZonedDateTime.parse(a._3.timestamp)), (a._1, a._3.side))).toMap
    val bars: util.List[ChartBar] = JavaConverters.seqAsJavaList(BackTestResults.candles1min.values.map(b => {
      ChartBar(b.key,b.high,b.low,b.open,b.close,orderMap.get(b.key).map(label).getOrElse(""))
    }).toSeq.sortBy(_.key))
    val values: util.List[(String, Int, BackTestResults.OrderResult, Int)] = JavaConverters.seqAsJavaList(orders.toSeq)

    val gson: Gson = new Gson()
    val json = gson.toJsonTree(ChartResponse(bars,values)).toString
    Ok(Json.parse(json)).withHeaders("Access-Control-Allow-Credentials" -> "true")
  }
  private def label(a:(String,String)): String = {
    val (inOut, side) = a
    (if (inOut == "entry") "E" else "C")  + side.substring(0, 1)
  }
  case class BackTestProps(start: String, end: String)

  def run() = withAuth { _ => implicit request: Request[AnyContent] =>
    val props: BackTestProps = form.bindFromRequest().get
    val start = DateUtil.of(props.start)
    val end = DateUtil.of(props.end)
    Future {
      app.run(start, end)
    } (scala.concurrent.ExecutionContext.Implicits.global)
    val gson:Gson = new Gson()
    Ok(Json.parse(gson.toJsonTree(props).toString)).withHeaders("Access-Control-Allow-Credentials" -> "true")
  }
}
