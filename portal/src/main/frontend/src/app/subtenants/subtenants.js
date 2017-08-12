/*globals alert */
(function() {
    'use strict';

    angular
      .module('portal')
      .controller('SubtenantsController', SubtenantsController)
      .controller('CreateSubtenantController', SubtenantsController);

    /** @ngInject */
    function SubtenantsController($log, $rootScope, TenantResource, $mdDialog) {
      var ctrl = this;
      $log.debug("user data: " + $rootScope.userData);
      ctrl.userData = $rootScope.userData;

      if (ctrl.userData) {
        ctrl.tenant = TenantResource.get();
        ctrl.tenant.$promise.then(
          function() {
            var pom = ctrl.tenant.quota - ctrl.tenant.usedQuota - 1;
            if (pom < 0) {
              pom = 0;
            }
            ctrl.repositoryData = [(pom / 1024).toFixed(1), (ctrl.tenant.usedQuota / 1024).toFixed(1)];
          },
          function(error) {
            alert(error.data.message);
          });
      }

      ctrl.createSubtenant = function() {
        var modalInstance = $mdDialog.show({
          //animation: false,
          templateUrl: 'createSubtenant.html',
          controller: 'CreateSubtenantController as ctrl',
          parent: angular.element(document.body),
          clickOutsideToClose: true,
          fullscreen: $rootScope.customFullscreen,
          locals: {
            tenant: ctrl.tenant
          }
        });
    };



    ctrl.getItems();

  }

  /** @ngInject */
  function CreateSubtenantController($log, $mdDialog, SubtenantResource) {
    var ctrl = this;

    ctrl.subtenant = {};
    ctrl.quota = 0;

    ctrl.save = function () {
      ctrl.subtenant.quota = ctrl.quota * 1024;
      ctrl.promise = SubtenantResource.save(ctrl.subtenant);
      ctrl.promise.$promise.then(
        function () {
          $mdDialog.hide('success');
        },
        function (error) {
          alert(error.data.message);
        });

    };

    ctrl.cancel = function () {
      $mdDialog.hide('cancel');
    };
  }

})();
