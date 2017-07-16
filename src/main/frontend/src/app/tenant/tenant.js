/*globals alert */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('TenantsController', TenantsController)
    .controller('RenameController', RenameController)
    .controller('SubtenantsController', SubtenantsController);

  /** @ngInject */
  function TenantsController($log, TenantResource, $uibModal) {
    var ctrl = this;

    ctrl.actionKey = null;

    ctrl.getItems = function () {
      ctrl.tenants = TenantResource.query();
      ctrl.tenants.$promise.then(
        function () {
          //$log.debug(ctrl.tenants);
        },
        function (error) {
          alert(error.data.message);
        });
    };

    ctrl.action = function (tenant) {
      $log.debug(ctrl.actionKey + " " + tenant);
      if (ctrl.actionKey === 'rename') {
        var modalInstance = $uibModal.open({
          animation: false,
          templateUrl: 'rename.html',
          controller: 'RenameController as ctrl',
          resolve: {
            tenant: function () {
              return tenant;
            }
          }
        });

        modalInstance.result.then(function () {
          ctrl.getItems();
        });
      }
    };

    ctrl.getItems();
  }

  /** @ngInject */
  function SubtenantsController($log, $routeParams, SubtenantResource, TenantResource) {
    var ctrl = this;

    ctrl.tenant =  $routeParams.uid;
    ctrl.tenants = TenantResource.query();

    ctrl.getItems = function () {
      if (ctrl.tenant) {
        ctrl.subtenants = SubtenantResource.query({uid: ctrl.tenant});
        ctrl.subtenants.$promise.then(
          function () {
            //$log.debug(ctrl.subtenants);
          },
          function (error) {
            alert(error.data.message);
          });
      }
    };

    ctrl.getItems();
  }

  /** @ngInject */
  function RenameController($log, tenant, $modalInstance, TenantResource) {
    var ctrl = this;

    ctrl.tenant = tenant;

    ctrl.save = function () {
      TenantResource.save(ctrl.tenant).$promise.then(
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
})();
