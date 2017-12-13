package repository.model.scalatrader

case class User(id:Long,
                email:String,
                password:String,
                name:String,
                api_key:String,
                api_secret:String)

case class CurrentOrder(
  id:Long,
  email:String,
  child_order_acceptance_id:String,
  side: String,
  size: Double,
  timestamp:String
) {
  import domain.Side._
  def reverseSide: String = if (Buy == side) Sell else Buy
}