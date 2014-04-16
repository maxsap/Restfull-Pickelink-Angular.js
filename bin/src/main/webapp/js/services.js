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
.factory('SessionResource', ['$resource', function($resource) {

    return $resource('service/session/:dest', {}, {
  	login: {method: 'POST', params: {dest:"login"}},
  	logout: {method: 'POST', params: {dest:"login"}}
    });

    return resource;
  }])
.factory('UsersResource', ['$resource', function($resource) {

  // Easy way to template urls and reuse them later
  return $resource('service/users/:dest', {}, {
	registration: {method: 'POST', params: {dest:"registration"}},
	activation: {method: 'POST', params: {dest:"activation"}}
  });

  return resource;
}])
// Define the object to hold user state on the client
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
}]);

//angular.module('membersService', ['ngResource', 'ngRoute']).
//factory('Members', function($resource){
//    return $resource('service/members:memberId', {});
//});