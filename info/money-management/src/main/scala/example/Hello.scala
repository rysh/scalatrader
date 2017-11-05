package example

object Hello extends Greeting with App {
  println(greeting)
}

trait Greeting {
  lazy val greeting: String = "hello"
}

/*
// ACCESS-SIGN は、ACCESS-TIMESTAMP, HTTP メソッド, リクエストのパス, リクエストボディ を文字列として連結したものを、 API secret で HMAC-SHA256 署名を行った結果です。

// Node.js のサンプル
var request = require('request');
var crypto = require('crypto');

var key = '{{ YOUR API KEY }}';
var secret = '{{ YOUR API SECRET }}';

var timestamp = Date.now().toString();
var method = 'POST';
var path = '/v1/me/sendchildorder';
var body = JSON.stringify({
    product_code: 'BTC_JPY',
    child_order_type: 'LIMIT',
    side: 'BUY',
    price: 30000,
    size: 0.1
});

var text = timestamp + method + path + body;
var sign = crypto.createHmac('sha256', secret).update(text).digest('hex');

var options = {
    url: 'https://api.bitflyer.jp' + path,
    method: method,
    body: body,
    headers: {
        'ACCESS-KEY': key,
        'ACCESS-TIMESTAMP': timestamp,
        'ACCESS-SIGN': sign,
        'Content-Type': 'application/json'
    }
};
 */