package controllers

import javax.inject._

import application.settings.UserApplication
import domain.user.Settings
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

@Singleton
class SettingsController @Inject()(cc: ControllerComponents, configuration: play.api.Configuration) extends AbstractController(cc) with Secured {

  def settings() = withAuth { email => implicit request: Request[AnyContent] =>
    try {
      val settings = UserApplication.getSettings(email, configuration.underlying.getString("play.http.secret.key"))
      Ok(views.html.settings(settings.getOrElse(Settings("", "", ""))))
    } catch {
      case e: Exception => BadRequest(views.html.settings(Settings("", "", "")))
    }
  }

  val form = Form(
    mapping(
      "name" -> text,
      "key" -> nonEmptyText,
      "secret" -> nonEmptyText
    )(Settings.apply)(Settings.unapply)
  )

  def update() = withAuth { email => implicit request: Request[AnyContent] =>
    val settings: Settings = form.bindFromRequest().get
    UserApplication.update(email, settings, configuration.underlying.getString("play.http.secret.key"))
    Ok(views.html.settings(settings))
  }
}
