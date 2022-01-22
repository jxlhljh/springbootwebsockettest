function WebsocketClient() {
};

WebsocketClient.prototype._generateEndpoint = function () {
    if (window.location.protocol == 'https:') {
        var protocol = 'wss://';
    } else {
        var protocol = 'ws://';
    }
    
    //var endpoint = 'ws://127.0.0.1:8080/websocket';
    var endpoint = protocol+ window.location.host + '/websocket';
    return endpoint;
};

WebsocketClient.prototype.connect = function (options) {
    var endpoint = this._generateEndpoint();

    if (window.WebSocket) {
        //如果支持websocket
        this._connection = new WebSocket(endpoint);
        //设置为二进制消息
        this._connection.binaryType = 'arraybuffer';
    }else {
        //否则报错
        options.onError('WebSocket Not Supported');
        return;
    }

    this._connection.onopen = function () {
        options.onConnect();
    };

    this._connection.onmessage = function (evt) {
        //var data = evt.data.toString();
        //data = base64.decode(data);
        var data = evt.data;
        options.onData(data);
    };


    this._connection.onclose = function (evt) {
        options.onClose();
    };
};

WebsocketClient.prototype.send = function (data) {
    //this._connection.send(JSON.stringify(data));
    this._connection.send(data);
};

WebsocketClient.prototype.sendInitData = function (options) {
    //连接参数
    this._connection.send(JSON.stringify(options));
}

WebsocketClient.prototype.sendClientData = function (data) {
    //发送指令
    this._connection.send(JSON.stringify(data))
}