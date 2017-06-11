var PubNub = require('pubnub');
var AWS = require('aws-sdk');
var moment = require('moment');

var pubnub = new PubNub({
    subscribeKey: 'sub-c-52a9ab50-291b-11e5-baaa-0619f8945a4f'
});

var now = function () {
	return moment().format("YYYY/MM/DD/HH/mm");
}

var lacking = true;
var lastTime;
var pool;
var init = function () {
	pool = new Array();
	lastTime = now();
}
init();

pubnub.addListener({
    message: function (message) {
    	var msg = message.message;
        pool.push(new Array(
        	msg.timestamp,
        	msg.tick_id,
        	msg.best_bid,
        	msg.best_ask,
        	msg.best_bid_size,
        	msg.best_ask_size,
        	msg.total_bid_depth,
        	msg.total_ask_depth,
        	msg.ltp,msg.volume,
        	msg.volume_by_product).join(','));
    	if (lastTime != now()) {
    		store(lastTime, pool);
        	console.log(lastTime);
        	init();
    	}
    }
});

var s3 = new AWS.S3();
var store = function (name, data) {
	var params = {
		Bucket: 'btcfx-ticker', 
		Key: name + (lacking ? '_lacking' : ''), 
		Body: data.join('\n')};
	s3.upload(params, function(err, data) {
	  console.log(err, data);
	});
	lacking = false;
}

pubnub.subscribe({
    channels: ['lightning_ticker_FX_BTC_JPY']
});