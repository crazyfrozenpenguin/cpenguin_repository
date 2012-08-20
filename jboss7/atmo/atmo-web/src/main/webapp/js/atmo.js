var cm;
var cm_actions = {
	'ON':['showItem', '#cp-1'], 
	'OFF':['hideItem', '#cp-1'],
};

$(document).ready(function(){
	$('#cp-1').hide();
	
	cm = new ControlManager();
 });

function controlConnect() {
	controlUnsubscribe();
    controlSubscribe();
}

// jquery.atmosphere.response
function callback(response) {
	// Websocket events.
	$.atmosphere.log('info', ["response.state: " + response.state]);
	$.atmosphere.log('info', ["response.transport: " + response.transport]);
	$.atmosphere.log('info', ["response.status: " + response.status]);

	if (response.transport != 'polling' && response.state == 'messageReceived') {
		$.atmosphere.log('info', ["response.responseBody: " + response.responseBody]);
		if (response.status == 200) {
			var data = response.responseBody;
			if (data.length > 0) {
				for (var cmd in cm.actions) {
					if (data === cmd) {
						var m = window[cm.actions[cmd][0]];
						var ma;
						if (cm.actions[cmd].length > 1) ma = cm.actions[cmd][1];
						if (ma && ma!= '') m(ma);
						else m();
					}
				}
			}
		}
	}
}

function controlSubscribe() {
	if (!this.callbackAdded) {
		var url = $.url();
		var location = url.attr('protocol') + '://' + url.attr('host') + ':' + url.attr('port') + '/atmo-web/pubsub/control';
		this.connectedEndpoint = $.atmosphere.subscribe(location,
			!callbackAdded ? this.callback : null,
			$.atmosphere.request = { 
				transport: 'websocket' 
			}
		);
		
		this.callbackAdded = true;
	}	
}

function controlUnsubscribe(){
	this.callbackAdded = false;
	$.atmosphere.unsubscribe();
}

function ControlManager() {
	this.suscribe = controlSubscribe;
	this.unsubscribe = controlUnsubscribe;
	this.connect = controlConnect;
	this.callback = callback;
	this.connect();
	this.actions = cm_actions;
}

function showItem(id) {
	$(id).show();
}

function hideItem(id) {
	$(id).hide();
}

function post() {
	
	var request = $.ajax({
		url: 'rest/control/toggle',
		type: 'POST',
		data: '<doc>foo</doc>',
		contentType:"application/xml; charset=utf-8",
	});
				
	request.fail(function(jqXHR, textStatus) {
  		alert( "Request failed: " + textStatus );
	});
}
