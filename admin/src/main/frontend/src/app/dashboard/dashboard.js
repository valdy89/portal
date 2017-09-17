/*globals alert */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('DashboardController', DashboardController);

  /** @ngInject */
  function DashboardController($log, $http, $rootScope, RepositoryResource, EndpointConfigService) {
    var ctrl = this;

    ctrl.repositories = RepositoryResource.query();
    ctrl.repositories.$promise.then(
      function () {
        //$log.debug(ctrl.tenants);
      },
      function (error) {
        alert(error.data.message);
      });

    ctrl.tenantsPromise = $http.get(EndpointConfigService.getUrl('/tenant/nocredit'));
    ctrl.tenantsPromise.then(function (response) {
       ctrl.tenants = response.data;
     }, function (response) {
       $log.debug(response);
       alert(response.data.message);
     });
  }

})();
