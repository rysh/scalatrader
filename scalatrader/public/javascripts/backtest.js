var dataPoints = [];

var chart;

window.onload = function () {
    chart = new CanvasJS.Chart("chartContainer", {
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

};

function renderChart(data) {

    for (var i = 0; i < data.length; i++) {
        if (data[i].length > 0) {
            var points = data[i];
            console.log(points);
            var date = new Date(
                parseInt(points[0].substr(0, 4)),
                parseInt(points[0].substr(4, 2)),
                parseInt(points[0].substr(6, 2)),
                parseInt(points[0].substr(8, 2)),
                parseInt(points[0].substr(10, 2))
            );
            var datum = {
                x: date,
                y: [
                    parseFloat(points[1]),
                    parseFloat(points[2]),
                    parseFloat(points[3]),
                    parseFloat(points[4])
                ],
                d: date.getMonth() + "/" + date.getDay() + " " + date.getHours() + ":" + date.getMinutes()
            };
            if ((i % 10) == 0) {
                datum.indexLabel = "BUY";
            }
            dataPoints.push(datum);
        }
    }
    chart.render();
}

function run() {
    console.log(new FormData(document.getElementById('backTestParameters')));
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
                console.log(json);
                var data = json.map(function (bar) {
                    return [String(bar.key), bar.open, bar.high, bar.low, bar.close]
                });
                console.log(data);
                renderChart(data);
            });
        } else {
            console.log(response.statusText); // => Error Message
        }
    });
}