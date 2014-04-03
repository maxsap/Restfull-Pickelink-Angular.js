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
// Define any routes for the app
// Note that this app is a single page app, and each partial is routed to using the URL fragment. For example, to select the 'home' route, the URL is http://localhost:8080/jboss-as-kitchensink-angularjs/#/home
var appModule = angular
	.module('kitchensink', [ 'membersService', 'productServices', 'LocalStorageModule' ])
	.config([ '$routeProvider', function($routeProvider) {
	    $routeProvider.
	    when('/home', {
		templateUrl : 'partials/home.html',
		controller : MembersCtrl,
		isFree: false
	    // Add a default route
	    }).when('/', {
                templateUrl: 'partials/login.html'
                    ,controller: 'LoginCtrl'
                    ,access: {
                        isFree: true
                    }
             }).when('/signup', {
                 templateUrl: 'partials/signup.html'
                     ,controller: 'SignupCtrl'
                     ,access: {
                         isFree: true
                     }
              }).otherwise({
		redirectTo : 'login'
	    });
	} ])
	.factory('authHttpResponseInterceptor',['$q','$location',function($q,$location){
                return {
                    response: function(response){
                        if (response.status === 401) {
                            $location.path('/');
                        }
                        return response || $q.when(response);
                    },
                    responseError: function(rejection) {
                        if (rejection.status === 401) {
                            $location.path('/');
                        }
                        return $q.reject(rejection);
                    }
                }
        }]).config(['$httpProvider',function($httpProvider) {
                //Http Intercpetor to check auth failures for xhr requests
                $httpProvider.interceptors.push('authHttpResponseInterceptor');
        }]).run( function($rootScope, $location) {

	    // register listener to watch route changes
	    $rootScope.$on( "$routeChangeStart", function(event, next, current) {
	      if ( $rootScope.loggedUser == null ) {
	        // no logged user, we should be going to #login
	        if ( next.templateUrl == "partials/login.html" ) {
	          // already going to #login, no redirect needed
	        } else {
	          // not going to #login, we should redirect now
	          // $location.path( "/login" );
	        }
	      }         
	    });
	 });


/**
 * Directive to check if the user have rights to see the resource. Use: <div
 * checkUser> ... </div>
 * 
 */
/*
 * appModule.directive('checkUser',['$rootScope', '$location', 'UserService',
 * 'localStorageService', 'CheckToken', '$http', function($root, $location,
 * localStorageService, CheckToken, $http) { return { link : function(scope,
 * elem, attrs, ctrl) { $root.$on('$routeChangeStart', function(event,
 * currRoute, prevRoute) { if ($http.defaults.headers.common['access_token'] ===
 * null || $http.defaults.headers.common['access_token'] === undefined) {
 * CheckToken.check(UserService.token).$promise.then(function(u) {
 */
					/*
					 * include code for every XHR request
					 * see:
					 * http://blog.brunoscopelliti.com/authentication-to-a-restful-web-service-in-an-angularjs-web-app
					 */ 
					/*
					 * $http.defaults.headers.common['access_token'] =
					 * u.access_token; $root.access_token =
					 * localStorageService.get('access_token');
					 * UserService.token =
					 * $root.access_token;
					 * UserService.isLogged = true;
					 * UserService.code = u.access_token;
					 * UserService.state = "logedin"; },
					 * function(u) {
					 */
					    /*
					     * on server error, this usually
					     * means that the token is invalid
					     */
					 /*
					     * if (!currRoute.access.isFree)
					     * $location.path('/login'); } ); }
					     * }); } } } ]);
					     */