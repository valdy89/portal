(function () {
  'use strict';

  angular
    .module('portal')
    .controller('MainController', MainController)
    .controller('ChangePasswordController', ChangePasswordController);

  /** @ngInject */
  function MainController($log, $cookies, $uibModal, $window, $rootScope, $location, AuthenticationService) {
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

    $rootScope.$on('userLoggedIn', function () {
      $log.debug("userLoggedIn: " + $rootScope.userData);
      main.userData = $rootScope.userData;
      main.navbarUrl = 'app/main/navbar.html';
    });

    $rootScope.$on('userLoggedOut', function () {
      main.userData =null;
      main.navbarUrl = null;
    });

    main.logout = function () {
      AuthenticationService.logout();
      $window.location.reload();
    };

    main.changePassword = function () {
      var modalInstance = $uibModal.open({
        animation: false,
        templateUrl: 'changePassword.html',
        controller: 'ChangePasswordController as ctrl',
        size: 'sm'
      });
    };
  }

  function ChangePasswordController($log, $http, $modalInstance, EndpointConfigService) {
    var ctrl = this;


    ctrl.save = function () {
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/login'), ctrl.password);
      ctrl.promise.then(
        function () {
          $modalInstance.close();
        },
        function (error) {
          alert(error.data.message);
        }
      );
    };

    ctrl.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  }

})
();
