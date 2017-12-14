package repository

import com.google.inject.Inject
import domain.util.crypto.Aes
import repository.model.scalatrader.{User, CurrentOrder}
import scalikejdbc.AutoSession
import scalikejdbc._

object UserRepository {

  def get(email: String, secret: String): Option[User] = {
    implicit val session = AutoSession
    sql"select * from user where email = ${email}".map(map(secret)).single().apply()
  }
  def all(secret: String): Seq[User] = {
    implicit val session = AutoSession
    sql"select * from user".map(map(secret)).list().apply()
  }

  def everyoneWithApiKey(secret: String): Seq[User] = {
    all(secret).filter(user => notEmpty(user.api_key) && notEmpty(user.api_secret))
  }

  def notEmpty(str: String): Boolean = {
    if (str == null) {
      false
    } else {
      str.length > 0
    }
  }

  def storeCurrentOrder(email: String, acceptanceId: String, side: String, size: Double): Int = {
    implicit val session = AutoSession
    sql"insert into current_position (email, child_order_acceptance_id, side, size) values ($email, $acceptanceId, $side, $size)".update.apply()
  }
  def clearCurrentOrder(email: String, acceptanceId: String): Int = {
    implicit val session = AutoSession
    sql"delete from current_position where email = $email and child_order_acceptance_id = $acceptanceId".update.apply()
  }
  def fetchCurrentOrder(): Seq[CurrentOrder] = {
    implicit val session = AutoSession
    sql"select * from current_position".map((rs: WrappedResultSet) => {
      CurrentOrder(rs.long("id"),
        rs.string("email"),
        rs.string("child_order_acceptance_id"),
        rs.string("side"),
        rs.double("size"),
        rs.string("timestamp"))
    }).list().apply()
  }

  private def map = {
    secret:String => (rs: WrappedResultSet) => {
      User(rs.long("id"),
        rs.string("email"),
        rs.string("password"),
        rs.string("name"),
        Aes.decode(rs.string("api_key"), secret),
        Aes.decode(rs.string("api_secret"), secret))
    }
  }
}