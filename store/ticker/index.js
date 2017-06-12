var PubNub = require('pubnub');
var AWS = require('aws-sdk');
var moment = require('moment');

var pubnub = new PubNub({
    subscribeKey: 'sub-c-52a9ab50-291b-11e5-baaa-0619f8945a4f'
});

var formatTimestamp = function (timestamp) {
	return moment(timestamp).format("YYYY/MM/DD/HH/mm");
}

var lacking = true;
var lastTime = formatTimestamp('');
var pool = new Array();
var init = function (time) {
	pool = new Array();
	lastTime = time;
}

pubnub.addListener({
    message: function (message) {
    	var msg = message.message;
        var msgTime = formatTimestamp(msg.timestamp);
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
    	if (lastTime != msgTime) {
    		store(lastTime, pool);
            lacking = false;
        	init(msgTime);
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
}

pubnub.subscribe({
    channels: ['lightning_ticker_FX_BTC_JPY']
});