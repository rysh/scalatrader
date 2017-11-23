package repository

import com.google.inject.Inject
import domain.util.crypto.Aes
import repository.model.scalatrader.User
import scalikejdbc.AutoSession
import scalikejdbc._

object UserRepository {

  def get(email: String, secret: String): Option[User] = {
    implicit val session = AutoSession
    sql"select email from user where email = ${email}".map(map(secret)).single().apply()
  }
  def all(secret: String): Seq[User] = {
    implicit val session = AutoSession
    sql"select * from user".map(map(secret)).list().apply()
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