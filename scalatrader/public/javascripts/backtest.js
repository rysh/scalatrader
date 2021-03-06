

window.onload = function () {
};

function run() {
    var r = jsRoutes.controllers.BackTestController.run();
    fetch(r.url, {
        method: r.type,
        body: new FormData(document.getElementById('backTestParameters')),
        credentials: 'include'
    }).then(function (response) {
        if (response.status === 200) {
            console.log(response.statusText); // => "OK"
        } else {
            console.log(response.statusText); // => Error Message
        }
    }).catch(function (response) {
        console.log(response); // => "TypeError: ~"
    });
}

function showChart() {
    var r = jsRoutes.controllers.BackTestController.chart();
    var p = fetch(r.url, {
        method: r.type,
        credentials: 'include'
    }).then(function (response) {
        if (response.status === 200) {
            response.json().then(function (json) {
                candleChart(json.bars);
                performanceChart(json.values);
                showOrders(json.values);
            });
        } else {
            console.log(response.statusText); // => Error Message
        }
    });
}


function ticker() {
    var r = jsRoutes.controllers.BackTestController.ticker();
    fetch(r.url, {
        method: r.type,
        body: new FormData(document.getElementById('tickerParameters')),
        credentials: 'include'
    }).then(function (response) {
        if (response.status === 200) {
            response.json().then(function (json) {
                tickerChart(json);
            });
        } else {
            console.log(response.statusText); // => Error Message
        }
    });
}

function momentum() {
    var r = jsRoutes.controllers.BackTestController.momentum();
    fetch(r.url, {
        method: r.type,
        body: new FormData(document.getElementById('tickerParameters')),
        credentials: 'include'
    }).then(function (response) {
        if (response.status === 200) {
            response.json().then(function (json) {
                momentumChart(json);
            });
        } else {
            console.log(response.statusText); // => Error Message
        }
    });
}


function showOrders(values){
    var table = document.getElementById('orders');
    var child;
    while (child = table.lastChild) table.removeChild(child);
    values.forEach(function(a) {
        var tr = document.createElement('tr');
        var td = document.createElement('td');
        td.textContent = a._3.timestamp;
        tr.appendChild(td);
        var td2 = document.createElement('td');
        td2.textContent = a._3.side;
        tr.appendChild(td2);
        var td3 = document.createElement('td');
        td3.textContent = a._3.price;
        tr.appendChild(td3);
        var td4 = document.createElement('td');
        td4.textContent = a._4;
        tr.appendChild(td4);
        table.appendChild(tr);
    });

}

function candleChart(bars) {
    var data = bars.map(function (bar) {
        return [bar.timestamp, bar.open, bar.high, bar.low, bar.close, bar.label]
    });
    var dataPoints = [];
    var chart = new CanvasJS.Chart("chartContainer", {
        animationEnabled: true,
        theme: "light2", // "light1", "light2", "dark1", "dark2"
        exportEnabled: true,
        title: {
            text: "BTC-FX"
        },
        subtitles: [{
            text: "1min"
        }],
        axisX: {
            interval: 1,
            valueFormatString: "DD HH:mm"
        },
        axisY: {
            includeZero: false,
            prefix: "",
            title: "Price"
        },
        toolTip: {
            content: "Date: {d}<br /><strong>Price:</strong><br />Open: {y[0]}, Close: {y[3]}<br />High: {y[1]}, Low: {y[2]}"
        },
        data: [{
            type: "candlestick",
            yValueFormatString: "##0",
            dataPoints: dataPoints
        }]
    });

    for (var i = 0; i < data.length; i++) {
        if (data[i].length > 0) {
            var points = data[i];
            var date = new Date(points[0]);
            var datum = {
                x: date,
                y: [points[1], points[2], points[3], points[4]],
                d: date.getMonth() + "/" + date.getDay() + " " + date.getHours() + ":" + date.getMinutes(),
                indexLabel: points[5]
            };
            dataPoints.push(datum);
        }
    }
    chart.render();
}

function performanceChart(values) {
    var data = values.map(function (a) {
        return {x: new Date(a.timestamp), y: a.value}
    });
    var chart = new CanvasJS.Chart("chartContainer2", {
        animationEnabled: true,
        title: {
            text: "累積損益"
        },
        axisX: {
            minimum: data[0].x,
            maximum: data[data.length - 1].x,
            valueFormatString: "DD HH:mm"
        },
        axisY: {
            title: "Performance",
            titleFontColor: "#4F81BC",
            suffix: ""
        },
        data: [{
            indexLabelFontColor: "darkSlateGray",
            name: "views",
            type: "area",
            yValueFormatString: "#,##0",
            dataPoints: data
        }]
    });
    chart.render();
}

function tickerChart(tickers) {
    var data = tickers.map(function (e) {
        return {x:new Date(e._1), y:e._2, d: e._1}
    });
    var chart = new CanvasJS.Chart("chartContainer3", {
        animationEnabled: true,
        zoomEnabled: true,
        title:{
            text: "Ticker data"
        },
        axisX: {
            title:"Tickers",
            minimum: data[0].x,
            maximum: data[data.length - 1].x,
            valueFormatString: "DD HH:mm"
        },
        axisY:{
            title: "Price",
            includeZero: false,
            valueFormatString: "#,##0"
        },
        data: [{
            type: "scatter",
            toolTipContent: "<b>Timestamp: </b>{d}<br/><b>Price: </b>{y}",
            dataPoints:data
        }]
    });
    chart.render();
}
function momentumChart(momentums) {

    var data = momentums.map(function (e) {
        return {x:new Date(e.timestamp), y:e.value}
    });
    var chart = new CanvasJS.Chart("chartContainer4", {
        animationEnabled: true,
        zoomEnabled: true,
        title:{
            text: "Momentum data"
        },
        axisX: {
            title:"Date",
            minimum: data[0].x,
            maximum: data[data.length - 1].x,
            valueFormatString: "DD HH:mm"
        },
        axisY:{
            title: "Momentum",
            includeZero: false,
            valueFormatString: "#,##0"
        },
        data: [{
            type: "line",
            toolTipContent: "<b>Timestamp: </b>{d}<br/><b>Price: </b>{y}",
            dataPoints:data
        }]
    });
    chart.render();
}