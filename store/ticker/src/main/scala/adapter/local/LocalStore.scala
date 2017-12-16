package adapter.local

import adapter.Store
import com.google.gson.JsonElement
import domain.{NamingRule, TimeKeeper}

/**
  *
  * Created by ryuhei.ishibashi on 2017/07/06.
  */
class LocalStore(
  val fileNamePrefix: String = "tmp",
  override val timeKeeper:TimeKeeper = TimeKeeper.default()
) extends Store(timeKeeper) {

  /** インスタンス生成したときにファイルを作成する */
  val namingRule = new NamingRule(fileNamePrefix)

  lazy val fileName = namingRule.path(timeKeeper)
  lazy val file = better.files.File(fileName).createIfNotExists()


  override def store(json: JsonElement): Unit = {
    writeJson(json)
  }

  override def write(): Either[Unit, Store] = {
    if (!timeKeeper.nowElapsed) {
      return Left()
    }
    Right(new LocalStore(fileNamePrefix, timeKeeper.next))
  }

  def writeJson(json: JsonElement): Unit = file.appendLines(json.toString)

  def lines(): Traversable[String] = file.lines;

  def delete(): Either[Exception, Unit] = try { Right(file.delete()) } catch {case e:Exception => Left(e)}
  def exists(): Boolean = file.exists

}
