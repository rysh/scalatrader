

window.onload = function () {
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