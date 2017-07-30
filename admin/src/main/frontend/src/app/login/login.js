/*globals alert */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('LoginController', LoginController)
    .factory('AuthenticationService', AuthenticationService);

  /** @ngInject */
  function LoginController($log, $cookies, $http, $base64, $rootScope, $location, AuthenticationService, EndpointConfigService) {
    var login = this;

    //AuthenticationService.logout();

    login.login = function () {
      login.dataLoading = true;
      $log.debug("Login roles");
      var userData = {username: login.username, authdata: $base64.encode(login.username + ':' + login.password)};
      $http.defaults.headers.common['Authorization'] = 'Basic ' + userData.authdata; // jshint ignore:line
      $http.get(EndpointConfigService.getUrl('/login')).then(function (response) {
        $log.debug("Login user data: " + response);
        userData = response.data;
        $rootScope.userData = userData;
        $cookies.putObject('userData', userData);
        $rootScope.$broadcast('userLoggedIn', userData);
        $location.path('/');
      }, function (response) {
        login.dataLoading = false;
        $log.debug(response);
        if (response.data) {
          alert(response.data.message);
        } else {
          alert("Server not responding, please try action again later.");
        }
      });
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
