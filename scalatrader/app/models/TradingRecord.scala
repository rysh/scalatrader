package models

import scalikejdbc._
import java.time.{ZonedDateTime}

case class TradingRecord(
  id: Long,
  email: String,
  tradingRuleStateId: Long,
  entryId: String,
  entryExecution: Option[Any] = None,
  entryTimestamp: ZonedDateTime,
  closeId: Option[String] = None,
  closeExecution: Option[Any] = None,
  closeTimestamp: Option[ZonedDateTime] = None,
  timestamp: ZonedDateTime) {

  def save()(implicit session: DBSession = TradingRecord.autoSession): TradingRecord = TradingRecord.save(this)(session)

  def destroy()(implicit session: DBSession = TradingRecord.autoSession): Int = TradingRecord.destroy(this)(session)

}


object TradingRecord extends SQLSyntaxSupport[TradingRecord] {

  override val schemaName = Some("scalatrader")

  override val tableName = "trading_record"

  override val columns = Seq("id", "email", "trading_rule_state_id", "entry_id", "entry_execution", "entry_timestamp", "close_id", "close_execution", "close_timestamp", "timestamp")

  def apply(tr: SyntaxProvider[TradingRecord])(rs: WrappedResultSet): TradingRecord = apply(tr.resultName)(rs)
  def apply(tr: ResultName[TradingRecord])(rs: WrappedResultSet): TradingRecord = new TradingRecord(
    id = rs.get(tr.id),
    email = rs.get(tr.email),
    tradingRuleStateId = rs.get(tr.tradingRuleStateId),
    entryId = rs.get(tr.entryId),
    entryExecution = rs.anyOpt(tr.entryExecution),
    entryTimestamp = rs.get(tr.entryTimestamp),
    closeId = rs.get(tr.closeId),
    closeExecution = rs.anyOpt(tr.closeExecution),
    closeTimestamp = rs.get(tr.closeTimestamp),
    timestamp = rs.get(tr.timestamp)
  )

  val tr = TradingRecord.syntax("tr")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[TradingRecord] = {
    withSQL {
      select.from(TradingRecord as tr).where.eq(tr.id, id)
    }.map(TradingRecord(tr.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[TradingRecord] = {
    withSQL(select.from(TradingRecord as tr)).map(TradingRecord(tr.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(TradingRecord as tr)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[TradingRecord] = {
    withSQL {
      select.from(TradingRecord as tr).where.append(where)
    }.map(TradingRecord(tr.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[TradingRecord] = {
    withSQL {
      select.from(TradingRecord as tr).where.append(where)
    }.map(TradingRecord(tr.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(TradingRecord as tr).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    email: String,
    tradingRuleStateId: Long,
    entryId: String,
    entryExecution: Option[Any] = None,
    entryTimestamp: ZonedDateTime,
    closeId: Option[String] = None,
    closeExecution: Option[Any] = None,
    closeTimestamp: Option[ZonedDateTime] = None,
    timestamp: ZonedDateTime)(implicit session: DBSession = autoSession): TradingRecord = {
    val generatedKey = withSQL {
      insert.into(TradingRecord).namedValues(
        column.email -> email,
        column.tradingRuleStateId -> tradingRuleStateId,
        column.entryId -> entryId,
        (column.entryExecution, ParameterBinder(entryExecution, (ps, i) => ps.setObject(i, entryExecution))),
        column.entryTimestamp -> entryTimestamp,
        column.closeId -> closeId,
        (column.closeExecution, ParameterBinder(closeExecution, (ps, i) => ps.setObject(i, closeExecution))),
        column.closeTimestamp -> closeTimestamp,
        column.timestamp -> timestamp
      )
    }.updateAndReturnGeneratedKey.apply()

    TradingRecord(
      id = generatedKey,
      email = email,
      tradingRuleStateId = tradingRuleStateId,
      entryId = entryId,
      entryExecution = entryExecution,
      entryTimestamp = entryTimestamp,
      closeId = closeId,
      closeExecution = closeExecution,
      closeTimestamp = closeTimestamp,
      timestamp = timestamp)
  }

  def batchInsert(entities: Seq[TradingRecord])(implicit session: DBSession = autoSession): List[Int] = {
    val params: Seq[Seq[(Symbol, Any)]] = entities.map(entity =>
      Seq(
        'email -> entity.email,
        'tradingRuleStateId -> entity.tradingRuleStateId,
        'entryId -> entity.entryId,
        'entryExecution -> entity.entryExecution,
        'entryTimestamp -> entity.entryTimestamp,
        'closeId -> entity.closeId,
        'closeExecution -> entity.closeExecution,
        'closeTimestamp -> entity.closeTimestamp,
        'timestamp -> entity.timestamp))
    SQL("""insert into trading_record(
      email,
      trading_rule_state_id,
      entry_id,
      entry_execution,
      entry_timestamp,
      close_id,
      close_execution,
      close_timestamp,
      timestamp
    ) values (
      {email},
      {tradingRuleStateId},
      {entryId},
      {entryExecution},
      {entryTimestamp},
      {closeId},
      {closeExecution},
      {closeTimestamp},
      {timestamp}
    )""").batchByName(params: _*).apply[List]()
  }

  def save(entity: TradingRecord)(implicit session: DBSession = autoSession): TradingRecord = {
    withSQL {
      update(TradingRecord).set(
        column.id -> entity.id,
        column.email -> entity.email,
        column.tradingRuleStateId -> entity.tradingRuleStateId,
        column.entryId -> entity.entryId,
        (column.entryExecution, ParameterBinder(entity.entryExecution, (ps, i) => ps.setObject(i, entity.entryExecution))),
        column.entryTimestamp -> entity.entryTimestamp,
        column.closeId -> entity.closeId,
        (column.closeExecution, ParameterBinder(entity.closeExecution, (ps, i) => ps.setObject(i, entity.closeExecution))),
        column.closeTimestamp -> entity.closeTimestamp,
        column.timestamp -> entity.timestamp
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: TradingRecord)(implicit session: DBSession = autoSession): Int = {
    withSQL { delete.from(TradingRecord).where.eq(column.id, entity.id) }.update.apply()
  }

}
