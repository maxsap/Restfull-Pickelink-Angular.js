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

angular.module('productServices', ['ngResource', 'ngRoute'])
.factory('Product', ['$resource', function($resource){
  var Product = $resource('/api/products/:id', {  }, {
    update: { method: 'PUT' }
  });

  return Product;
}])
// http://stackoverflow.com/questions/15161349/multiple-routing-urls-for-single-service-angularjs
//.factory('UsersResource', ['$resource', function ($resource) {
//    return $resource('rest/users/:dest', {}, {
//	login: {method: 'POST', params: {dest:"login"}},
//	token: {method: 'POST', params: {dest:"token"}}
//    });
//}])
.factory('TokenHandler', function() {
  var tokenHandler = {};
  var token = "none";

  tokenHandler.set = function( newToken ) {
    token = newToken;
  };

  tokenHandler.get = function() {
    return token;
  };

  // wrap given actions of a resource to send auth token with every
  // request
  tokenHandler.wrapActions = function( resource, actions ) {
    // copy original resource
    var wrappedResource = resource;
    for (var i=0; i < actions.length; i++) {
      tokenWrapper( wrappedResource, actions[i] );
    };
    // return modified copy of resource
    return wrappedResource;
  };

  // wraps resource action to send request with auth token
  var tokenWrapper = function( resource, action ) {
    // copy original action
    resource['_' + action]  = resource[action];
    // create new action wrapping the original and sending token
    resource[action] = function( data, success, error){
      return resource['_' + action](
        angular.extend({}, data || {}, {access_token: tokenHandler.get()}),
        success,
        error
      );
    };
  };

  return tokenHandler;
})
.factory('UsersResource', ['$resource', 'TokenHandler', function($resource, tokenHandler) {

  return $resource('rest/users/:dest', {}, {
	login: {method: 'POST', params: {dest:"login"}},
	registration: {method: 'POST', params: {dest:"registration"}},
	token: {method: 'POST', params: {dest:"token"}}
  });

//  resource = tokenHandler.wrapActions( resource, ["query", "update"] );

  return resource;
}])
.factory('UserService', [function () {
    var sdo = {
        isLogged: false,
        username: '',
        code:"",
        token:"",
        state:"",
        expires:""
    };
    return sdo;
}])
.factory('Auth', ['localStorageService', '$http',
	function(localStorageService, $http) {
	    $http.defaults.headers.common['Authorization'] = 'Basic ' + localStorageService.get('access_token');
	    return {
		setCredentials : function(token) {
		    $http.defaults.headers.common.Authorization = 'Basic ' + token;
		},
		clearCredentials : function() {
		    localStorageService.remove('access_token');
		}
	    };
} ])

angular.module('membersService', ['ngResource', 'ngRoute']).
factory('Members', function($resource){
    return $resource('rest/members:memberId', {});
});