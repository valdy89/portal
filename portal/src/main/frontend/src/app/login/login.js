/*globals alert */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('LoginController', LoginController)
    .controller('ForgotPassworController', ForgotPassworController)
    .factory('AuthenticationService', AuthenticationService);


  /** @ngInject */
  function LoginController($log, $cookies, $http, $base64, $rootScope, $location, $mdDialog, EndpointConfigService) {
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
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/register'), ctrl.user);
      ctrl.promise.then(function () {
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
      $log.debug(ctrl.username);
      $mdDialog.show({
        //animation: false,
        templateUrl: 'forgotPassword.html',
        controller: 'ForgotPassworController as ctrl',
        parent: angular.element(document.body),
        clickOutsideToClose: true,
        fullscreen: $rootScope.customFullscreen,
        locals: {
          username: ctrl.username
        }
      });
    };
  }

  /** @ngInject */
  function ForgotPassworController($log, $mdDialog, $http, EndpointConfigService, username) {
    var ctrl = this;

    ctrl.username = username;

    ctrl.save = function () {
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/forgotPassword'), {username: ctrl.username});
      ctrl.promise.then(function () {
        $mdDialog.hide();
      }, function (response) {
        alert(response.data.message);
      });

    };

    ctrl.cancel = function () {
      $mdDialog.hide();
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
      $cookies.remove('JSESSIONID');
      $cookies.remove('userData');
      $http.defaults.headers.common.Authorization = null;
    }

    return service;
  }

})();
