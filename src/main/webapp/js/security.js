angular.module('PicketLinkSecurity', ['ngResource', 'ngRoute']).config(
    [ '$routeProvider', function($routeProvider) {
        $routeProvider.when('/login', {
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
                isFree: true
            }
        }).when('/successfulRegistration', {
            templateUrl : 'partials/successfulRegistration.html',
            access : {
                isFree : true
            }
        }).when('/invalidActivationCode', {
            templateUrl : 'partials/invalidActivationCode.html',
            access : {
                isFree : true
            }
        });
    } ])
    .factory('SessionResource', ['$resource', function($resource) {
        return $resource('rest/:dest', {}, {
            login: {method: 'POST', params: {dest:"authc"}},
            logout: {method: 'POST', params: {dest:"logout"}}
        });
    }])
     .factory('AdminResource', ['$resource', function($resource) {
        return $resource('rest/admin/:dest', {}, {
            activate: {method: 'POST', params: {dest:"activate"}},
        });
    }])
    .factory('UsersResource', ['$resource', function($resource) {
        return $resource('rest/users/:dest', {}, {});
    }])
    .factory('RegistrationResource', ['$resource', function($resource) {
        return $resource('rest/register/:dest', {}, {
            activation: {method: 'POST', params: {dest:"activation"}}
        });
    }])
    .factory('SecurityService', ['$rootScope', function($rootScope) {

        var SecurityService = function() {
            this.initSession = function(response) {
                console.log("[INFO] Initializing user session.");
                console.log("[INFO] Token is :" + response.token);
                console.log("[INFO] Token Stored in session storage.");
                // persist token, user id to the storage
                sessionStorage.setItem('token', response.token);
            };

            this.endSession = function() {
                console.log("[INFO] Ending User Session.");
                sessionStorage.removeItem('token');
                console.log("[INFO] Token removed from session storage.");
            };

            this.getToken = function() {
                return sessionStorage.getItem('token');
            };

            this.secureRequest = function(requestConfig) {
                var token = this.getToken();

                if(token != null && token != '' && token != 'undefined') {
                    console.log("[INFO] Securing request.");
                    console.log("[INFO] Setting x-session-token header: " + token);
                    requestConfig.headers['x-session-token'] = token;
                }
            };
        };

        return new SecurityService();
    }]);

// controllers definition
function LoginCtrl($scope, SessionResource, SecurityService, $location) {
    $scope.newUser = {};
    $scope.isLoggedIn = false;

    $scope.login = function() {
        if ($scope.newUser.userId != undefined && $scope.newUser.password != undefined) {
            SessionResource.login($scope.newUser,
                function (data) {
        	    $scope.isLoggedIn = true;
                    SecurityService.initSession(data);
                    $location.path( "/home" );
                }
            );
        }
    };

    $scope.redirectoToSignUp = function() {
        $location.path( "/signup" );
    };

}

function SignupCtrl($scope, $http, RegistrationResource, $q, $location, $timeout) {
    $scope.register = function() {
        if($scope.newUser.password != $scope.newUser.passwordConfirmation) {
            $scope.errors = {passwordConfirmation : "Password Mismatch !!!"};
            return;
        }

        RegistrationResource.save($scope.newUser, function(data) {
            $location.path("/successfulRegistration");
        });
    };
}

function ActivationCtrl($scope, $routeParams, RegistrationResource, SecurityService, $location) {
    var ac = $routeParams.activationCode;
    $scope.activate = function() {
        RegistrationResource.activation(JSON.stringify(ac), function(data) {
            $scope.isLoggedIn = true;
            SecurityService.initSession(data);
            $location.path( "/login" );
        }, function(result) {
            $location.path( "/invalidActivationCode" );
        });

    };

    $scope.activate();
}

//angular.module("SignatureUtil", [])
//    .service("SignatureUtil", function() {
//        var jws = function() {
//            var hmacKey = "hmackey";
//
//            this.generateSignature = function(joeStr, hs256) {
//
//                var token = new jwt.WebToken(joeStr, hs256);
//                var signed = token.serialize(hmacKey)
//                var split = signed.split("\.")
//
//                return split;
//            };
//
//            this.verifySignature = function(signature) {
//                var token = jwt.WebTokenParser.parse(signature);
//                return token.verify(hmacKey);
//            };
//
//            this.getClaims = function(jwsEncoded) {
//                console.log("claims:" + jwsEncoded.split(".")[1]);
//                var claims = atob(jwsEncoded.split(".")[1]);
//                console.log(claims);
//                return claims;
//            };
//        }
//
//        return {
//            getInstance: function () {
//                return new jws();
//            }
//        };
//    });