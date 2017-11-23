package application

import org.scalatest.{BeforeAndAfter, FunSuite}
import scalikejdbc.ConnectionPool

class RegularObservationTest extends FunSuite with BeforeAndAfter {

  test("testSummary") {
    ConnectionPool.singleton("jdbc:mysql://localhost:6603/scalatrader", "root", "password")
    //(new RegularObservation(null)).summary("QCYtAnfkaZiwrNwnxIlR6CTfG3gf90Latabg5241ABR5W1uDFNIkn")
  }

}
