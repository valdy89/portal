(function () {
  'use strict';

  angular
    .module('portal')
    .controller('MainController', MainController);

  /** @ngInject */
  function MainController($log, $cookies, $http, $window, $scope, $injector, $rootScope, $location, AuthenticationService, EndpointConfigService) {
    var main = this;

    $log.debug("Main controller begin");

    if ($location.path() !== '/login' && !$rootScope.userData) {
      if ($cookies.getObject('userData')) {
        $rootScope.userData = $cookies.getObject('userData');
        $rootScope.$broadcast('userLoggedIn', $rootScope.userData);
        main.userData = $rootScope.userData;
        main.navbarUrl = 'app/main/navbar.html';
      }
    }

    $log.debug("Main userData: " + $rootScope.userData);

    if (!$rootScope.userData) {
      $location.path('/login');
    }

    $injector.invoke($rootScope.AlertController, main);

    main.hasAlert = false;

    $scope.$on('userLoggedIn', function () {
      $log.debug("userLoggedIn: " + $rootScope.userData);
      main.userData = $rootScope.userData;
      main.navbarUrl = 'app/main/navbar.html';
    });

    main.logout = function () {
      AuthenticationService.logout();
      $window.location.reload();
    };
  }

})
();
