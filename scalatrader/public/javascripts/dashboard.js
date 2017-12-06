

window.onload = function () {
};

function run() {
    var r = jsRoutes.controllers.DashBoardController.run();
    fetch(r.url, {
        method: r.type,
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

function stop() {
    var r = jsRoutes.controllers.DashBoardController.stop();
    fetch(r.url, {
        method: r.type,
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
