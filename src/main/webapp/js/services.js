/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Define the REST resource service, allowing us to interact with it as a high level service

angular.module('MessageModule', ['ngResource', 'ngRoute'])
.factory('MessageService', ['$rootScope', function($rootScope) {
    $rootScope.messages = [];

    var MessageService = function() {
        this.setMessages = function(messages) {
            console.log(messages);
            $rootScope.messages = messages;
        };

        this.hasMessages = function() {
            return $rootScope.messages && $rootScope.messages.length > 0;
        }

        this.clearMessages = function() {
            $rootScope.messages = [];
        }
    };

    return new MessageService();
}]);

/*
 * taken from http://clintberry.com/2013/angular-js-websocket-service/
 */                          
angular.module('WsUtilModule', ['ngResource', 'ngRoute']).factory('WsUtilService', ['$q', '$rootScope', 'SecurityService', function($q, $rootScope, SecurityService) {
    // We return this object to anything injecting our service
    var Service = {};
    // Keep all pending requests here until they get responses
    var callbacks = {};
    // Create a unique callback ID to map requests to responses
    var currentCallbackId = 0;
    
    var host = window.location.hostname;
    
    // Create our websocket object with the address to the websocket
    var ws = new WebSocket("wss://" + host + ":8443/" + "send?token=" + encodeURIComponent( SecurityService.getToken() ) );
    
    ws.onopen = function() { 
        console.log("Socket has been opened!");  
    };
    
    ws.onmessage = function(message) {
        listener(JSON.parse(message.data));
    };
    
    ws.onclose = function() { 
       // websocket is closed.
       console.log("Socket has been opened!");  
    };

    function sendRequest(request) {
      var defer = $q.defer();
      var callbackId = getCallbackId();
      callbacks[callbackId] = {
        time: new Date(),
        cb:defer
      };
      request.callback_id = callbackId;
      console.log('Sending request', request);
      ws.send(JSON.stringify(request));
      return defer.promise;
    }

    function listener(data) {
      var messageObj = data;
      console.log("Received data from websocket: ", messageObj);
      // If an object exists with callback_id in our callbacks object, resolve it
      if(callbacks.hasOwnProperty(messageObj.callback_id)) {
        console.log(callbacks[messageObj.callback_id]);
        $rootScope.$apply(callbacks[messageObj.callback_id].cb.resolve(messageObj));
        delete callbacks[messageObj.callbackID];
      }
    }
    // This creates a new callback ID for a request
    function getCallbackId() {
      currentCallbackId += 1;
      if(currentCallbackId > 10000) {
        currentCallbackId = 0;
      }
      return currentCallbackId;
    }

    // Define a "getter" for getting customer data
    Service.getCustomers = function() {
      var request = {
        type: "get_customers"
      }
      // Storing in a variable for clarity on what sendRequest returns
      var promise = sendRequest(request); 
      return promise;
    }

    return Service;
}]);