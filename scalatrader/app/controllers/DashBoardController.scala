package controllers

import java.time.ZonedDateTime

import javax.inject._
import application.{StrategySettingApplication, PerformanceViewApplication}
import com.google.gson.Gson
import domain.Side._
import domain.strategy.Strategies
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

import scala.collection.JavaConverters

@Singleton
class DashBoardController @Inject()(cc: ControllerComponents, strategySettingApplication: StrategySettingApplication, performanceApp: PerformanceViewApplication)
    extends AbstractController(cc)
    with Secured
    with play.api.i18n.I18nSupport {

  val strategies: Form[SystemSettings] = Form(
    mapping(
      "strategies" -> seq[StrategySettings](
        mapping(
          "id" -> longNumber,
          "name" -> text,
          "availability" -> boolean,
          "leverage" -> bigDecimal
        )(StrategySettings.apply)(StrategySettings.unapply))
    )(SystemSettings.apply)(SystemSettings.unapply))

  val newStrategy: Form[StrategySettings] = Form(
    mapping(
      "id" -> longNumber,
      "name" -> text,
      "availability" -> boolean,
      "leverage" -> bigDecimal
    )(StrategySettings.apply)(StrategySettings.unapply))

  val deleteTarget: Form[DeleteTarget] = Form(
    mapping(
      "id" -> longNumber
    )(DeleteTarget.apply)(DeleteTarget.unapply))

  val summaryRequest: Form[SummaryRequest] = Form(
    mapping(
      "strategyId" -> longNumber,
      "days" -> longNumber
    )(SummaryRequest.apply)(SummaryRequest.unapply))

  val positionRequest: Form[PositionRequest] = Form(
    mapping(
      "strategyId" -> longNumber,
    )(PositionRequest.apply)(PositionRequest.unapply))

  def main(): EssentialAction = withAuth { email => implicit request: Request[AnyContent] =>
    val isAvailable = Strategies.values.filter(_.email == email).exists(_.isAvailable)
    val status = if (isAvailable) "running" else "stopped"

    val settings = strategySettingApplication.get(email)
    Ok(views.html.dashboard(status, newStrategy, strategies.fill(SystemSettings(settings))))
  }

  def update(): EssentialAction = withAuth { email => implicit request: Request[AnyContent] =>
    val value: Form[SystemSettings] = strategies.bindFromRequest()
    val updatingStrategies = value.get.strategies
    strategySettingApplication.updateSetting(email, updatingStrategies)
    Ok(Json.parse("""{"status":"OK"}""")).withHeaders("Access-Control-Allow-Credentials" -> "true")
  }

  def add(): EssentialAction = withAuth { email => implicit request: Request[AnyContent] =>
    strategySettingApplication.add(email, newStrategy.bindFromRequest().get)
    Ok(Json.parse("""{"status":"OK"}""")).withHeaders("Access-Control-Allow-Credentials" -> "true")
  }

  def delete(): EssentialAction = withAuth { email => implicit request: Request[AnyContent] =>
    strategySettingApplication.delete(email, deleteTarget.bindFromRequest().get)
    Ok(Json.parse("""{"status":"OK"}""")).withHeaders("Access-Control-Allow-Credentials" -> "true")
  }
  def summary(): EssentialAction = withAuth { email => implicit request: Request[AnyContent] =>
    val params = summaryRequest.bindFromRequest().get
    val json = performanceApp
      .summary(email, params.strategyId, ZonedDateTime.now().minusDays(params.days))
      .map(r => {
        val gson: Gson = new Gson()
        gson.toJsonTree(r).toString
      })
      .getOrElse("{}")
    Ok(Json.parse(json)).withHeaders("Access-Control-Allow-Credentials" -> "true")
  }
  def position(): EssentialAction = withAuth { email => implicit request: Request[AnyContent] =>
    val params = positionRequest.bindFromRequest().get
    val size: PositionResponse = PositionResponse(
      Strategies.values
        .filter(_.email == email)
        .filter(_.state.id == params.strategyId)
        .flatMap(_.state.order.map(o => o.size * (if (o.side == Sell) -1 else 1)))
        .headOption
        .getOrElse(0))

    val gson: Gson = new Gson()

    Ok(Json.parse(gson.toJsonTree(size).toString)).withHeaders("Access-Control-Allow-Credentials" -> "true")
  }
}
case class SystemSettings(strategies: Seq[StrategySettings])
case class SummaryRequest(strategyId: Long, days: Long)
case class PositionRequest(strategyId: Long)
case class PositionResponse(size: Double)
case class StrategySettings(
    id: Long,
    name: String,
    availability: Boolean,
    leverage: BigDecimal
)
case class DeleteTarget(
    id: Long
)
