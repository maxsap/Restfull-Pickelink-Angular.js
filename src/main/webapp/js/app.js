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
var appModule = angular.module('PLAngular',
	[ 'productServices', 'LocalStorageModule', 'SignatureUtil']).config(
	[ '$routeProvider', function($routeProvider) {
	    $routeProvider.when('/home', {
		templateUrl : 'partials/home.html',
		controller : HomeCtrl,
		isFree : false
	    // Add a default route
	    }).when('/', {
		templateUrl : 'partials/login.html',
		controller : 'LoginCtrl',
		access : {
		    isFree : true
		}
	    }).when('/login', {
		templateUrl : 'partials/login.html',
		controller : 'LoginCtrl',
		access : {
		    isFree : true
		}
	    }).when('/signup', {
		templateUrl : 'partials/signup.html',
		controller : 'SignupCtrl',
		access : {
		    isFree : true
		}
	    }).when('/activate/:activationCode', {
		templateUrl : 'partials/activate.html',
		controller : 'ActivationCtrl',
		access : {
		    isFree : true
		}
	    }).otherwise({
		redirectTo : 'login'
	    });
	} ]).factory('authHttpResponseInterceptor', ['$q', '$location', 'UserService', 'localStorageService', 'SignatureUtil', function($q, $location, UserService, localStorageService, SignatureUtil) {
	    return {
		'request' : function(config) {
		    var token = localStorageService.get('token');
		    if(token != null && token != '')
			config.headers['x-session-token'] = token;
		    
		    var userId = localStorageService.get('uid');
		    if(userId != null && userId != '')
			config.headers['user-id'] = localStorageService.get('uid');
			
			var str = "{\"iss\":\"joe\",\r\n" + $location + ":true}";
			
			// XXX this should come from the API
			var hs256 = "{\"typ\":\"JWT\",\r\n"+
						 " \"alg\":\"HS256\"}";
			
			var res = SignatureUtil.getInstance().generateSignature(str, hs256);
			console.log(res);
			// XXX Token validation should be here
		    
		    return config || $q.when(config);
		},

		'requestError' : function(rejection) {
		    return $q.reject(rejection);
		},
		
		'response' : function(response) {
		    if (response.status === 401) {
				$location.path('/');
		    }
		    
		    var str = "eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJqb2UiLA0KW29iamVjdCBPYmplY3RdOnRydWV9.cOSM-mAHSz1DetLSO5X3My62LZU2cMMlm06f1kuc2tY";
		    // XXX read the actual value from the response and decode it!
		    var res = SignatureUtil.getInstance().verifySignature(str);
		    console.log(res);
		    // XXX check verification status
		    
		    return response || $q.when(response);
		},
		
		'responseError' : function(rejection) {
		    if (rejection.status === 401) {
			$location.path('/');
		    }
		    return $q.reject(rejection);
		}
	    }
	} ]).config([ '$httpProvider', function($httpProvider) {
            //Http Intercpetor to check auth failures for xhr requests
            $httpProvider.interceptors.push('authHttpResponseInterceptor');
        } ]).run(function($rootScope, $location) {

            // register listener to watch route changes
            $rootScope.$on("$routeChangeStart", function(event, next, current) {
        	if ($rootScope.loggedUser == null) {
        	    // no logged user, we should be going to #login
        	    if (next.templateUrl == "partials/login.html") {
        		// already going to #login, no redirect needed
        	    } else {
        		// not going to #login, we should redirect now
        		// $location.path( "/login" );
        	    }
        	}
            });
});
