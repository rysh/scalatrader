package controllers

import javax.inject._

import play.api.mvc._

@Singleton
class DashBoardController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with Secured {

  def main() = withAuth { _ => implicit request: Request[AnyContent] =>
    Ok(views.html.dashboard())
  }
}
