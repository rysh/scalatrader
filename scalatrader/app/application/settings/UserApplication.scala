package application.settings

import domain.user
import domain.user.Settings
import domain.util.crypto.{Md5, Aes}
import scalikejdbc._

object UserApplication {
// see http://scalikejdbc.org/

  def register(email:String, password: String) = {
    implicit val session = AutoSession
    sql"insert into user (email, password) values (${email}, ${Md5.hex(password)})".execute.apply()
  }

  def update(email: String, settings: Settings, secret: String): Boolean = {
    implicit val session = AutoSession
    sql"""update user set
          name = ${settings.name},
          api_key = ${Aes.encode(settings.key, Aes.makeKey(secret))},
          api_secret = ${Aes.encode(settings.secret, Aes.makeKey(secret))}
          where email = ${email}""".execute.apply()
  }

  def getSettings(email: String, secret: String): Option[Settings] = {
    implicit val session = AutoSession
    println(secret)
    sql"select name, api_key, api_secret from user where email = ${email}"
      .map(rs => Settings(rs.string("name"),
        Aes.decode(rs.string("api_key"), Aes.makeKey(secret)),
        Aes.decode(rs.string("api_secret"), Aes.makeKey(secret))))
      .single.apply()
  }

  def exists(email:String, password: String): Boolean = {
    implicit val session = AutoSession
    val hoge = sql"select email from user where email = ${email} and password = ${Md5.hex(password)}".map(rs => rs.string("email")).single().apply()
    hoge.isDefined
  }

  def delete(email:String, password: String): Unit = {
    implicit val session = AutoSession
    sql"delete from user where email = ${email} and password = ${Md5.hex(password)}".execute.apply()
  }
}
