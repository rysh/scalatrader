package repository

import domain.util.crypto.Aes
import repository.model.scalatrader.{User}
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

  private def map = { secret: String => (rs: WrappedResultSet) =>
    {
      User(
        rs.long("id"),
        rs.string("email"),
        rs.string("password"),
        rs.string("name"),
        Aes.decode(rs.string("api_key"), secret),
        Aes.decode(rs.string("api_secret"), secret)
      )
    }
  }
}
