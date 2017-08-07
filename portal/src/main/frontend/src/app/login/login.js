/*globals alert */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('LoginController', LoginController)
    .controller('ChangePasswordController', ChangePasswordController)
    .controller('ForgotPassworController', ForgotPassworController)
    .factory('AuthenticationService', AuthenticationService);


  /** @ngInject */
  function LoginController($log, $cookies, $http, $base64, $rootScope, $location, $uibModal, EndpointConfigService) {
    var ctrl = this;
    ctrl.activeForm = 'login';
    ctrl.panelLogin = 'active';
    ctrl.panelRegister = '';
    ctrl.user = {country: 'CZ'};

    ctrl.login = function () {
      var user = {
        username: ctrl.username,
        password: ctrl.password
      };
      ctrl.dataLoading = true;
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/login'), user);
      ctrl.promise.then(function (response) {
        $http.defaults.headers.common.Authorization = 'Basic ' + $base64.encode(ctrl.username + ':' + ctrl.password); // jshint ignore:line
        var userData = response.data;
        $rootScope.userData = userData;
        $cookies.putObject('userData', userData);
        $rootScope.$broadcast('userLoggedIn', userData);
        $location.path('/');
      }, function (response) {
        $http.defaults.headers.common.Authorization = null;
        if (response.data) {
          alert(response.data.message);
        } else {
          alert("Server not responding, please try action again later.");
        }
      });
    };

    ctrl.register = function () {
      $log.debug(ctrl.user);
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/register'), ctrl.user);
      ctrl.promise.then(function (response) {
        ctrl.panelLogin = 'active';
        ctrl.panelRegister = '';
        alert("Uživatel úspěšně zaregistrován, vyčkejte na verifikační email.");
      }, function (response) {
        if (response.data) {
          alert(response.data.message);
        } else {
          alert("Server not responding, please try action again later.");
        }
      });
    };

    ctrl.panelChange = function (page) {
      ctrl.activeForm = page;
    };

    ctrl.forgotPassword = function () {
      var modalInstance = $uibModal.open({
        animation: false,
        templateUrl: 'forgotPassword.html',
        controller: 'ForgotPassworController as ctrl'
      });
    }
  }

  /** @ngInject */
  function ForgotPassworController($log, $modalInstance, $http, EndpointConfigService) {
    var ctrl = this;

    ctrl.save = function () {
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/forgotPassword'), {username: ctrl.username});
      ctrl.promise.then(function (response) {
        $modalInstance.close();
      }, function (response) {
        alert(response.data.message);
      });

    };

    ctrl.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  }

  /** @ngInject */
  function ChangePasswordController($log, $routeParams, $cookies, $http, $base64, $rootScope, $location, EndpointConfigService,$mdDialog) {
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
        password: ctrl.password
      };
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/changePassword'), user);
      ctrl.promise.then(function (response) {
      }, function (response) {
        if (!response.data) {
          alert("Server not responding, please try action again later.");
        }
      });
    };

    ctrl.cancel = function () {
      $mdDialog.hide('cancel');
    };
  }

  /** @ngInject */
  function AuthenticationService($log, $cookies, $rootScope, $http, EndpointConfigService) {
    var service = this;

    service.logout = function () {
      $log.debug('logout');
      clearCredential();
      $http.get(EndpointConfigService.getAppUrl('/logout'));
    };

    function clearCredential() {
      $rootScope.userData = null;
      //$cookies.remove('userData');
      $cookies.remove('userData');
      $http.defaults.headers.common.Authorization = null;
    }

    return service;
  }

})();
