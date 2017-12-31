package application

import javax.inject.Inject

import com.google.inject.Singleton
import controllers.{StrategySettings, DeleteTarget}
import domain.models.Ordering
import domain.strategy.{StrategyState, Strategies, StrategyFactory}
import play.api.{Configuration, Logger}
import repository.{StrategyRepository, UserRepository}
import service.{InitializeService, StrategyStateService}


@Singleton
class StrategySettingApplication @Inject()(config: Configuration, strategyStateService: StrategyStateService) {

  val secret: String = config.get[String]("play.http.secret.key")

  def get(email:String): Seq[StrategySettings] = {
    val user = UserRepository.get(email, secret).get
    StrategyRepository.list(user).map(state => StrategySettings(state.id, state.name, state.availability, state.leverage))
  }

  def add(email:String, props: StrategySettings): Unit = {

    val state = StrategyState(0L, props.name, props.availability, props.leverage.toDouble, None, None)
    val user = UserRepository.get(email, secret).get
    StrategyRepository.store(user, state)
    val currentStrategies = Strategies.values.filter(st => st.email == user.email)
    StrategyRepository.list(user)
      .filter(st => !currentStrategies.exists(cur => cur.state.id == st.id && cur.email == user.email))
      .foreach(stateWithId => {
        Strategies.register(StrategyFactory.create(stateWithId, user))
    })
  }

  def updateSetting(email: String, updatingStrategies: Seq[StrategySettings]): Unit = {
    val user = UserRepository.get(email, secret).get
    StrategyRepository.list(user).foreach((currentState: StrategyState) => {
      updatingStrategies.find(_.id == currentState.id) match {
        case Some(setting) =>
          val newState = if (currentState.availability && !setting.availability) {
            strategyStateService.reverseOrder(user, currentState)
            currentState.copy(
              availability = setting.availability,
              leverage = setting.leverage.toDouble,
              order = None,
              orderId = None)
          } else {
            currentState.copy(
              availability = setting.availability,
              leverage = setting.leverage.toDouble)
          }
          Strategies.update(newState)
          StrategyRepository.update(user, newState)
        case None =>
      }
    })
  }

  def updateOrder(email: String, currentState: StrategyState, orderId: Option[String], order: Option[Ordering]): Unit = {
    val user = UserRepository.get(email, secret).get
    val newState = StrategyState(
      currentState.id,
      currentState.name,
      currentState.availability,
      currentState.leverage.toDouble,
      orderId,
      order,
      currentState.params)
    Strategies.update(newState)
    StrategyRepository.update(user, newState)
  }

  def delete(email: String, target: DeleteTarget): Unit = {
    val user = UserRepository.get(email, secret).get
    StrategyRepository.get(user, target.id) match {
      case Some(state) =>
        println("setting delete")
        StrategyRepository.delete(user, target.id)
        Strategies.remove(user, target.id)
        if (state.order.isDefined) {
          strategyStateService.reverseOrder(user,  state)
        }
      case None =>
    }
  }
}
