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
function MembersCtrl($scope, $http, UsersResource, UserService, localStorageService, $q, $location, $timeout) {
    
    // Define a refresh function, that updates the data from the REST service
    $scope.refresh = function() {
        $scope.users = UsersResource.query();
    };

    // Define a reset function, that clears the prototype newMember object, and
    // consequently, the form
    $scope.reset = function() {
        // clear input fields
        $scope.newUser = {};
    };

    // Define a register function, which adds the member using the REST service,
    // and displays any error messages
    $scope.register = function() {
        $scope.successMessages = '';
        $scope.errorMessages = '';
        $scope.errors = {};

        Members.save($scope.newMember, function(data) {

            // mark success on the registration form
            $scope.successMessages = [ 'Member Registered' ];

            // Update the list of members
            $scope.refresh();

            // Clear the form
            $scope.reset();
        }, function(result) {
            if ((result.status == 409) || (result.status == 400)) {
                $scope.errors = result.data;
            } else {
                $scope.errorMessages = [ 'Unknown  server error' ];
            }
            $scope.$apply();
        });

    };

    // Call the refresh() function, to populate the list of members
    $scope.refresh();

    // Initialize newMember here to prevent Angular from sending a request
    // without a proper Content-Type.
    $scope.reset();

    // Set the default orderBy to the name property
    $scope.orderBy = 'name';
}


function LoginCtrl(Product, $rootScope, $scope, $http, UserService, UsersResource, $location, $q, localStorageService) {
    $scope.isLogged = UserService.isLogged;
    
    /**
     * Save a person. Make sure that a person object is present before calling the service.
     */
    $scope.dologin = function (userData) {
        if (userData.userId != undefined && userData.password != undefined) {

            UsersResource.login(userData, function (u) {
        	    console.log("Auth");
                    UserService.isLogged = true;
                    UserService.code = u.code;
                    UserService.state = u.state;
                    $location.path( "/home" );
                }, function (err) {
                    console.log(err.data.errorMessage);
                }
            );
        }
    };
    
    // when user whant's to sign-up for the service
    $scope.redirectoToSignUp = function() {
	$location.path( "/signup" );
    };

}


function SignupCtrl($scope, $http, UsersResource, UserService, localStorageService, $q, $location, $timeout) {

    // Define a register function, which adds the member using the REST service,
    // and displays any error messages
    $scope.register = function() {
        $scope.successMessages = '';
        $scope.errorMessages = '';
        $scope.errors = {};

        UsersResource.save($scope.newUser, function(data) {

            // mark success on the registration form
            $scope.successMessages = [ 'User Registered' ];

            $location.path( "/" );
        }, function(result) {
            if ((result.status == 409) || (result.status == 400)) {
                $scope.errors = result.data;
            } else {
                $scope.errorMessages = result.data;
            }
            $scope.$apply();
        });

    };
}


function HomeAppCtrl($scope, localStorageService, UserService) {
    $scope.access_token =   localStorageService.get('access_token');
    UserService.isLogged = ($scope.access_token)?1:0;
    UserService.token = $scope.access_token;
}