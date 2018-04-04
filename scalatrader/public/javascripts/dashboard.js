

window.onload = function () {
    performance()
};

function add() {
    var r = jsRoutes.controllers.DashBoardController.add();
    fetch(r.url, {
        method: r.type,
        body: new FormData(document.getElementById('newStrategy')),
        credentials: 'include'
    }).then(function (response) {
        if (response.status === 200) {
            console.log(response.statusText); // => "OK"
            location.reload();
        } else {
            console.log(response.statusText); // => Error Message
        }
    }).catch(function (response) {
        console.log(response); // => "TypeError: ~"
    });
}

function submit() {
    var r = jsRoutes.controllers.DashBoardController.update();
    fetch(r.url, {
        method: r.type,
        body: new FormData(document.getElementById('strategies')),
        credentials: 'include'
    }).then(function (response) {
        if (response.status === 200) {
            console.log(response.statusText); // => "OK"
            location.reload();
        } else {
            console.log(response.statusText); // => Error Message
        }
    }).catch(function (response) {
        console.log(response); // => "TypeError: ~"
    });
}

function deleteStrategy(id) {
    var r = jsRoutes.controllers.DashBoardController.delete();
    fetch(r.url, {
        method: r.type,
        body: new FormData(document.getElementById('delete-' + id)),
        credentials: 'include'
    }).then(function (response) {
        if (response.status === 200) {
            console.log(response.statusText); // => "OK"
            location.reload();
        } else {
            console.log(response.statusText); // => Error Message
        }
    }).catch(function (response) {
        console.log(response); // => "TypeError: ~"
    });
}

function summary(id, days, func) {
    var r = jsRoutes.controllers.DashBoardController.summary();
    fetch(r.url + "?strategyId=" + id + "&days=" + days, {
        method: r.type,
        credentials: 'include'
    }).then(function (response) {
        if (response.status === 200) {
            response.json().then(function (json) {
                func(json);
            });
        } else {
            console.log(response.statusText); // => Error Message
        }
    }).catch(function (response) {
        console.log(response); // => "TypeError: ~"
    });
}

function performance() {
    var days = document.getElementById('days').value

    var nodes = document.querySelectorAll('input.availability:checked');
    Array.from(nodes,  function (e) {
        var checkbox = e.parentNode.parentNode;
        var parent = checkbox.previousElementSibling.previousElementSibling;
        var strategyId = parent.querySelector('input').value;
        //api
        summary(strategyId, days, function(json) {
            var str = "{total: " + json.total
                + ", average: " + json.average
                + ", maxDD: " + json.maxDD
                + ", count: " + json.count + "}";
            e.parentNode.parentNode.parentNode.querySelector('.performance').innerHTML = str;
        });
    })
}