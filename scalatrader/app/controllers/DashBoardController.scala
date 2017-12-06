package controllers

import javax.inject._

import domain.strategy.Strategies
import play.api.mvc._

@Singleton
class DashBoardController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with Secured {

  def main() = withAuth { email => implicit request: Request[AnyContent] =>
    val isAvailable = Strategies.values.find(_.email == email).map(s => s.isAvailable).getOrElse(false)
    val status = if (isAvailable) "running" else "stopped"
    Ok(views.html.dashboard(status))
  }

  def run() = withAuth { email => implicit request: Request[AnyContent] =>
    Strategies.init()
    Strategies.values.filter(_.email == email).foreach(s => s.isAvailable = true)
    Ok("OK")
  }

  def stop() = withAuth { email => implicit request: Request[AnyContent] =>
    Strategies.values.filter(_.email == email).foreach(s => s.isAvailable = false)
    Ok("OK")
  }
}
