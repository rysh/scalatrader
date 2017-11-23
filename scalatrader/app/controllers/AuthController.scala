package controllers

import javax.inject._

import application.settings.UserApplication
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._

@Singleton
class AuthController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  case class LoginData(email: String, password: String)
  val form = Form(
    mapping(
      "email" -> email ,
      "password" -> nonEmptyText
    )(LoginData.apply)(LoginData.unapply)
  )

  def login() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.login())
  }

  def signUp() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.signup())
  }

  def authenticate() = Action { implicit request: Request[AnyContent] =>
    form.bindFromRequest()(request).fold(
      formWithErrors => BadRequest(views.html.login()),
      loginData => {
        if (UserApplication.exists(loginData.email, loginData.password)) {
          Redirect(routes.DashBoardController.main()).withSession("session.email" -> loginData.email)
        } else {
          BadRequest(views.html.login())
        }
      }
    )
  }

  def logout = Action {
    Redirect(routes.AuthController.login).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }

  def signUpAction() = Action { implicit request: Request[AnyContent] =>

    form.bindFromRequest()(request).fold(
      formWithErrors => {
        BadRequest(views.html.login())
      },
      loginData => {
        try {
          UserApplication.register(loginData.email, loginData.password)
          Redirect(routes.SettingsController.settings()).withSession("session.email" -> loginData.email)
        } catch {
          case e: Exception => BadRequest(views.html.login())
        }
      }
    )
  }
}
