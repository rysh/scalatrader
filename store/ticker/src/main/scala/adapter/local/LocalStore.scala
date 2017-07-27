package adapter.local

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import adapter.Store
import better.files.File
import com.google.gson.JsonElement
import domain.TimeKeeper

/**
  *
  * Created by ryuhei.ishibashi on 2017/07/06.
  */
class LocalStore(fileNamePrefix: String, tk:TimeKeeper = new TimeKeeper(1)) extends Store(tk) {

  /** インスタンス生成したときにファイルを作成する */
  lazy val fileName = localName(timeKeeper.time)
  println(fileName)
  private lazy val file = fileOfTime()

  override def store(json: JsonElement): Unit = {
    writeJson(json)
  }

  override def write(): Either[Unit, Store] = {
    if (!timeKeeper.nowElapsed) {
      return Left()
    }
    Right(new LocalStore(fileNamePrefix, timeKeeper.next))
  }


  // TODO 中身がから
  def writeJson(json: JsonElement): Unit ={
    import better.files.Dsl.SymbolicOperations
    file << json.toString
  }
  def lines(): Traversable[String] = file.lines;

  def delete(): Either[Exception, Unit] = try { Right(file.delete()) } catch {case e:Exception => Left(e)}
  def exists(): Boolean = file.exists


  private def fileOfTime(): File = {
    import better.files._
    File(fileName).createIfNotExists()
  }

  private def localName(now: LocalDateTime):String =  fileNamePrefix + now.format(DateTimeFormatter.ofPattern("yyyy_MM_dd_hh_mm"))



}
