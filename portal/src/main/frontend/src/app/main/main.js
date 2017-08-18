/*globals alert, document */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('MainController', MainController)
    .controller('ChangePasswordController', ChangePasswordController)
    .controller('ChangeUserController', ChangeUserController);

  /** @ngInject */
  function MainController($log, $cookies, $scope, $http, $rootScope, $location, EndpointConfigService, $mdDialog) {
    var main = this;

    $log.debug("Main controller begin");

    if ($location.path() !== '/login' && !$rootScope.userData) {
      main.promise = $http.get(EndpointConfigService.getUrl('/token'));
      main.promise.then(function (response) {
        $rootScope.userData = response.data;
        $rootScope.$broadcast('userLoggedIn', $rootScope.userData);
        $location.path('/');
      }, function (response) {
        // $http.defaults.headers.common.Authorization = null;
        // if (response.data) {
        //   alert(response.data.message);
        // } else {
        //   alert("Server not responding, please try action again later.");
        // }
        // $location.path("/login");
      });
    } else {
      $log.debug("Main userData: " + $rootScope.userData);
      if (!$rootScope.userData && $location.path() !== '/verify') {
        $location.path('/login');
      }
    }

    main.hasAlert = false;

    $scope.$on('userLoggedIn', function () {
      $log.debug("userLoggedIn: " + $rootScope.userData);
      main.userData = $rootScope.userData;
      main.navbarUrl = 'app/main/navbar.html';
    });

    main.logout = function () {
      $http.post(EndpointConfigService.getAppUrl('/logout'), {}).finally(function () {
        $http.defaults.headers.common.Authorization = undefined;
        $rootScope.userData = undefined;
        main.navbarUrl = undefined;
        $location.path("/login");
      });
    };

    main.changePassword = function () {
      var modalInstance = $mdDialog.show({
        // animation: false,
        templateUrl: 'changePasswordInternal.html',
        controller: 'ChangePasswordController as ctrl',
        parent: angular.element(document.body),
        clickOutsideToClose: true,
        fullscreen: $rootScope.customFullscreen
      });


    };
    main.changeInvoiceInput = function () {
      var modalInstance = $mdDialog.show({
        // animation: false,
        templateUrl: 'changeInvoiceInput.html',
        controller: 'ChangeUserController as ctrl',
        parent: angular.element(document.body),
        clickOutsideToClose: true,
        fullscreen: $rootScope.customFullscreen
      });


    };
  }

  /** @ngInject */
  function ChangePasswordController($log, $routeParams, $cookies, $http, $base64, $rootScope, $location, EndpointConfigService, $mdDialog) {
    var ctrl = this;

    ctrl.login = function () {
      var user = {
        code: $routeParams.code,
        password: ctrl.password
      };
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/verify'), user);
      ctrl.promise.then(function (response) {
        var userData = response.data;
        $http.defaults.headers.common.Authorization = 'Basic ' + $base64.encode(userData.username + ':' + ctrl.password); // jshint ignore:line
        $rootScope.userData = userData;
        $cookies.putObject('userData', userData);
        $rootScope.$broadcast('userLoggedIn', userData);
        $location.path('/');
      }, function (response) {
        if (response.data) {
          alert(response.data.message);
        } else {
          alert("Server not responding, please try action again later.");
        }
      });
    };


    ctrl.change = function () {
      var user = {
        password: ctrl.password,
        oldPassword: ctrl.oldPassword
      };
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/changePassword'), user);
      ctrl.promise.then(function (response) {
        $mdDialog.hide('cancel');
      }, function (response) {
        if (response.data) {
          alert(response.data.message);
        } else {
          alert("Server not responding, please try action again later.");
        }
      });
    };

    ctrl.cancel = function () {
      $mdDialog.hide('cancel');
    };
  }

  /** @ngInject */
  function ChangeUserController($log, UserResource, $mdDialog) {
    var ctrl = this;

    ctrl.user = UserResource.get();
    ctrl.user.$promise.then(function (response) {
    }, function (response) {
      if (response.data) {
        alert(response.data.message);
      } else {
        alert("Server not responding, please try action again later.");
      }
    });

    // save function
    ctrl.change = function () {
      ctrl.user = UserResource.save(ctrl.user);
      ctrl.user.$promise.then(function (response) {
        $mdDialog.hide('cancel');
      }, function (response) {
        if (response.data) {
          alert(response.data.message);
        } else {
          alert("Server not responding, please try action again later.");
        }
      });
    };

    ctrl.cancel = function () {
      $mdDialog.hide('cancel');
    };

  }

})
();
