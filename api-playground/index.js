var request = require('request');
var crypto = require('crypto');

var key = '*********';
var secret = '***********';

var timestamp = Date.now().toString();
console.log(timestamp)
var method = 'GET';
var path = '/v1/me/getpositions?product_code=FX_BTC_JPY';
var text = timestamp + method + path;
console.log(text)
var sign = crypto.createHmac('sha256', secret).update(text).digest('hex');
console.log(sign)
var options = {
    url: 'https://api.bitflyer.jp' + path,
    method: method,
    headers: {
        'ACCESS-KEY': key,
        'ACCESS-TIMESTAMP': timestamp,
        'ACCESS-SIGN': sign
    }
};
request(options, function (err, response, payload) {
    console.log(payload);
});
