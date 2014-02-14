
function CallSignalling(listener) {
	this.socket = null;
	this.listener = listener;
	var socket = io.connect();
	
	var self = this;
	socket.on('connect', function() {
		console.log("CS: connected")
		if(self.listener)
			self.listener.swfReady("ru.rcslabs.webcall.SignalTransport");
	});
	socket.on('message', function(message) {

		if(typeof message === "string"){
			message = JSON.parse(message)
		}

		var type = message.type;
		delete message.type;
		if(self.listener)
			self.listener.webcallMessageHandler(type, message);
	});

	this.socket = socket;
}
CallSignalling.prototype = {
	sendMessage : function (type, params) {
		throw "Not implemented";
	},
	getVersion : function () {
		return "0.0.1";
	},
	storage : function () {
		throw "Not implemented";
	},
	webcallRequest : function (type, params) {
		if(!params) params = {};
		params.type = type;

		params.service = 'click2call';

		this.socket.emit('message', params);
	},
	setClientInfo : function (prop, value) {
		throw "Not implemented";
	}
}

