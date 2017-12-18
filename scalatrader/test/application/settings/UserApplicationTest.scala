package application.settings

import org.scalatest.FunSuite
import scalikejdbc.ConnectionPool

class UserApplicationTest extends FunSuite {

  ignore("testRegister") {
    ConnectionPool.singleton("jdbc:mysql://localhost:6603/scalatrader", "root", "password")
    UserApplication.register("fuga","fuga")
    UserApplication.delete("fuga","fuga")
  }

  ignore("testExistsUser") {
    ConnectionPool.singleton("jdbc:mysql://localhost:6603/scalatrader", "root", "password")
    assert(UserApplication.exists("hoge", "hoge"))
  }
}
