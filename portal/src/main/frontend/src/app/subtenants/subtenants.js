/*globals alert */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('SubtenantsController', SubtenantsController);

  /** @ngInject */
  function SubtenantsController($log, $rootScope, TenantResource, $mdDialog, $mdToast) {
    var ctrl = this;
    $log.debug("user data: " + $rootScope.userData);
    ctrl.userData = $rootScope.userData;

    if (ctrl.userData) {
      ctrl.tenant = TenantResource.get();
      console.log(ctrl.tenant);
      ctrl.tenant.$promise.then(
        function () {

          ctrl.repositoryData = [(pom / 1024).toFixed(1), (ctrl.tenant.usedQuota / 1024).toFixed(1)];
        },
        function (error) {
          alert(error.data.message);
        });
  }
}
})();
