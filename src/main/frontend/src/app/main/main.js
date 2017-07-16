(function () {
  'use strict';

  angular
    .module('portal')
    .controller('MainController', MainController);

  /** @ngInject */
  function MainController($log, $http, $window, $scope, $injector, $rootScope, $location, AuthenticationService, EndpointConfigService) {
    var main = this;

    $log.debug("Main controller");

    if ($location.path() !== '/login') {
      if ($http.defaults.headers.common.Authorization) {
        main.rolesPromise = $http.get(EndpointConfigService.getUrl('/login'));
        main.rolesPromise.then(function (response) {
          var userData = response.data[0];
          $rootScope.userData = userData;
          $rootScope.$broadcast('userLoggedIn', userData);
          //$location.path('/');
        }, function (response) {
          $location.path('/login');
          //alert(response.data.message);
          $log.debug(response);
          return;
        });
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
      main.menuUrl = 'app/main/menu.html';
    });

    main.logout = function () {
      AuthenticationService.logout();
      //$window.location.reload();
    };
  }

})
();
