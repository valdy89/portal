/*globals alert */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('DashboardController', DashboardController);

  /** @ngInject */
  function DashboardController($log, $rootScope, RepositoryResource) {
    var ctrl = this;

    $log.debug("user data: " + $rootScope.userData);
    ctrl.userData = $rootScope.userData;

    ctrl.repositories = RepositoryResource.query();

    ctrl.repositories.$promise.then(
      function () {
        //$log.debug(ctrl.tenants);
      },
      function (error) {
        alert(error.data.message);
      });

    // ctrl.messagePromise = $http.get(EndpointConfigService.getUrl('/dashboard'));
    // ctrl.messagePromise.then(function (response) {
    //   ctrl.message = response.data;
    // }, function (response) {
    //   $log.debug(response);
    //   alert(response.data.message);
    // });
  }

})();
