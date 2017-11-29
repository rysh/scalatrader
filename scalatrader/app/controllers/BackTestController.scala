package controllers

import java.time.format.DateTimeFormatter.ofPattern
import java.time.{ZonedDateTime, LocalDateTime, ZoneId}
import javax.inject._

import application.BackTestApplication
import application.settings.UserApplication
import com.google.gson.Gson
import domain.strategy.turtle.{Bar, BackTestResults}
import domain.time.DateUtil
import domain.user.Settings
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

  def chart() = withAuth { _ => implicit request: Request[AnyContent] =>
    val gson:Gson = new Gson()
    val bars: Seq[Bar] = BackTestResults.candles1min.values.toSeq.sortBy(_.key)
    val json = gson.toJsonTree(JavaConverters.seqAsJavaList(bars)).toString
    println(json)
    Ok(Json.parse(json)).withHeaders("Access-Control-Allow-Credentials" -> "true")
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
