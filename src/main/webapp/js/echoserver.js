var wsUrl;

var appPath = window.location.pathname.split('/')[1];
var host = window.location.hostname;
var port = "8443";

if (host == 'localhost') {
	port = '8443';
}


window.onload = init;



var websocket;
function init() {
  initWebSocket();

  var sendButton = document.getElementById("sendMsg");
  sendButton.onclick = sendToServer;
}

function sendToServer() {
  var chatMsgText = document.getElementById("chatMsg");
  var chatMsg = chatMsgText.value;
  if (chatMsg) {

    websocket.send(chatMsg);
    document.getElementById("chatMsgInfo").innerHTML = " ";
  }
  else {
    document.getElementById("chatMsgInfo").innerHTML = "Enter some message to be sent";

  }
}

/**
 * Function to init the WebSocket connection to the server endpoint
 */
function initWebSocket() {
  //If the websocket is not intialized, then create a new one.
  if (websocket == null) {
    websocket = new WebSocket('wss://' + host + ':'+ port +'/' + 'Project/send');
  }
  else if (websocket != null) {
    //The websocket might have been closed, so reconnect to the server.
    if (websocket.readyState != WebSocket.OPEN) {
      websocket = new WebSocket('wss://' + host + ':'+ port +'/' + 'Project/send');
    }
  }

  websocket.onmessage = function(event) {
    var serverMsgs = document.getElementById("serverMsg");
    console.log(event.data);
    var msgObj = JSON.parse(event.data);
    var dt = document.createElement("dt");
    dt.innerHTML = msgObj.time;
    serverMsgs.appendChild(dt);
    var dd = document.createElement("dd");
    dd.innerHTML = msgObj.message;
    serverMsgs.appendChild(dd);
  }
}