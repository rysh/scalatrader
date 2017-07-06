import application.ReceiveTicker

object Main extends App {


  ReceiveTicker.start("lightning_ticker_FX_BTC_JPY", "sub-c-52a9ab50-291b-11e5-baaa-0619f8945a4f")
}

