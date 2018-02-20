package controllers

import javax.inject._

import application.StrategySettingApplication
import domain.strategy.Strategies
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

@Singleton
class DashBoardController @Inject()(cc: ControllerComponents, strategySettingApplication: StrategySettingApplication) extends AbstractController(cc) with Secured  with play.api.i18n.I18nSupport{

  val strategies :Form[SystemSettings] = Form(
    mapping(
      "strategies" -> seq[StrategySettings](mapping(
        "id" -> longNumber,
        "name" -> text,
        "availability" -> boolean,
        "leverage" -> bigDecimal
      )(StrategySettings.apply)(StrategySettings.unapply))
    )(SystemSettings.apply)(SystemSettings.unapply))

  val newStrategy :Form[StrategySettings] = Form(
    mapping(
        "id" -> longNumber,
        "name" -> text,
        "availability" -> boolean,
        "leverage" -> bigDecimal
      )(StrategySettings.apply)(StrategySettings.unapply))

  val deleteTarget :Form[DeleteTarget] = Form(
    mapping(
      "id" -> longNumber
    )(DeleteTarget.apply)(DeleteTarget.unapply))

  def main(): EssentialAction = withAuth { email =>implicit request: Request[AnyContent] =>
    val isAvailable = Strategies.values.filter(_.email == email).exists(_.isAvailable)
    val status = if (isAvailable) "running" else "stopped"

    val settings = strategySettingApplication.get(email)
    Ok(views.html.dashboard(status, newStrategy, strategies.fill(SystemSettings(settings))))
  }

  def update(): EssentialAction = withAuth { email =>implicit request: Request[AnyContent] =>
    val value: Form[SystemSettings] = strategies.bindFromRequest()
    val updatingStrategies = value.get.strategies
    strategySettingApplication.updateSetting(email, updatingStrategies)
    Ok(Json.parse("""{"status":"OK"}""")).withHeaders("Access-Control-Allow-Credentials" -> "true")
  }

  def add(): EssentialAction = withAuth { email =>implicit request: Request[AnyContent] =>
    strategySettingApplication.add(email, newStrategy.bindFromRequest().get)
    Ok(Json.parse("""{"status":"OK"}""")).withHeaders("Access-Control-Allow-Credentials" -> "true")
  }

  def delete(): EssentialAction = withAuth { email =>implicit request: Request[AnyContent] =>
    strategySettingApplication.delete(email, deleteTarget.bindFromRequest().get)
    Ok(Json.parse("""{"status":"OK"}""")).withHeaders("Access-Control-Allow-Credentials" -> "true")
  }
}
case class SystemSettings(strategies: Seq[StrategySettings])
case class StrategySettings(
  id: Long,
  name: String,
  availability: Boolean,
  leverage: BigDecimal
)
case class DeleteTarget(
  id:Long
)
