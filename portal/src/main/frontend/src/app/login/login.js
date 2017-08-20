/*globals document */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('LoginController', LoginController)
    .controller('ForgotPassworController', ForgotPassworController);


  /** @ngInject */
  function LoginController($log, $http, $rootScope, $location, $mdDialog, $base64, $cookies, EndpointConfigService, ErrorHandlerService) {
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
        $rootScope.$broadcast('userLoggedIn', $rootScope.userData);
        $location.path('/');
      }, function (response) {
        $http.defaults.headers.common.Authorization = null;
        ErrorHandlerService.handleError(response);
      });
    };

    ctrl.register = function () {
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/register'), ctrl.user);
      ctrl.promise.then(function () {
        ctrl.panelLogin = 'active';
        ctrl.panelRegister = '';
        var alert = $mdDialog.alert()
          .title('Potvrzení regitrace')
          .textContent('Uživatel úspěšně zaregistrován, vyčkejte na verifikační email.')
          .ok('Zavřít');
        $mdDialog.show(alert);
      }, function (response) {
        ErrorHandlerService.handleError(response);
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
  function ForgotPassworController($log, $mdDialog, $http, EndpointConfigService, ErrorHandlerService, username) {
    var ctrl = this;

    ctrl.username = username;

    ctrl.save = function () {
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/forgotPassword'), {username: ctrl.username});
      ctrl.promise.then(function () {
        $mdDialog.hide();
      }, function (response) {
        ErrorHandlerService.handleError(response);
      });

    };

    ctrl.cancel = function () {
      $mdDialog.hide();
    };
  }

})();
