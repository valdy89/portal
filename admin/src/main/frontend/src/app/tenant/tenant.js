/*globals alert */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('TenantsController', TenantsController)
    .controller('ChangeQuotaController', ChangeQuotaController)
    .controller('ChangeTenantController', ChangeTenantController)
    .controller('SubtenantsController', SubtenantsController)
    .controller('SubChangeQuotaController', SubChangeQuotaController)
    .controller('SubChangePasswordController', SubChangePasswordController);

  /** @ngInject */
  function TenantsController($log, $uibModal, TenantResource) {
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
      if (ctrl.actionKey === 'changePassword') {
        var modalInstance = $uibModal.open({
          animation: false,
          templateUrl: 'tenantChangePassword.html',
          controller: 'ChangeTenantController as ctrl',
          resolve: {
            tenant: function () {
              return tenant;
            }
          }
        });

        modalInstance.result.then(function () {
          ctrl.getItems();
        });
      } else if (ctrl.actionKey === 'changeQuota') {
        var modalInstance = $uibModal.open({
          animation: false,
          templateUrl: 'tenantChangeQuota.html',
          controller: 'ChangeQuotaController as ctrl',
          resolve: {
            tenant: function () {
              return tenant;
            }
          }
        });

        modalInstance.result.then(function () {
          ctrl.getItems();
        });
      } else if (ctrl.actionKey === 'changeCredit') {
        var modalInstance = $uibModal.open({
          animation: false,
          templateUrl: 'tenantChangeCredit.html',
          controller: 'ChangeTenantController as ctrl',
          resolve: {
            tenant: function () {
              return tenant;
            }
          }
        });

        modalInstance.result.then(function () {
          ctrl.getItems();
        });
      } else if (ctrl.actionKey === 'enable') {
        tenant.enabled = true;
        ctrl.promise = TenantResource.save(tenant).$promise;
        ctrl.promise.then(function (response) {
          ctrl.getItems();
        }, function (response) {
          alert(response.data.message);
        });
      } else if (ctrl.actionKey === 'disable') {
        tenant.enabled = false;
        ctrl.promise = TenantResource.save(tenant).$promise;
        ctrl.promise.then(function (response) {
          ctrl.getItems();
        }, function (response) {
          alert(response.data.message);
        });
      } else if (ctrl.actionKey === 'addVip') {
        tenant.vip = true;
        ctrl.promise = TenantResource.save(tenant).$promise;
        ctrl.promise.then(function (response) {
          ctrl.getItems();
        }, function (response) {
          alert(response.data.message);
        });
      } else if (ctrl.actionKey === 'removeVip') {
        tenant.vip = false;
        ctrl.promise = TenantResource.save(tenant).$promise;
        ctrl.promise.then(function (response) {
          ctrl.getItems();
        }, function (response) {
          alert(response.data.message);
        });
      } else if (ctrl.actionKey === 'delete') {
        ctrl.promise = TenantResource.delete({id: tenant.id}).$promise;
        ctrl.promise.then(function (response) {
          ctrl.getItems();
        }, function (response) {
          alert(response.data.message);
        });
      }
      ctrl.actionKey = null;
    };

    ctrl.getItems();
  }

  /** @ngInject */
  function ChangeTenantController($log, $modalInstance, tenant, TenantResource) {
    var ctrl = this;

    ctrl.tenant = tenant;

    ctrl.save = function () {
      ctrl.promise = TenantResource.save(ctrl.tenant).$promise
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

  /** @ngInject */
  function ChangeQuotaController($log, $modalInstance, tenant, TenantResource) {
    var ctrl = this;

    ctrl.tenant = tenant;
    ctrl.quota = ctrl.tenant.quota / 1024;

    ctrl.save = function () {
      ctrl.tenant.quota = ctrl.quota * 1024;
      ctrl.promise = TenantResource.save(ctrl.tenant).$promise;
      ctrl.promise.then(function (response) {
        $modalInstance.close();
      }, function (response) {
        alert(response.data.message);
      });

    };

    ctrl.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  }


  /** @ngInject */
  function SubtenantsController($log, $uibModal, $routeParams, SubtenantResource, TenantResource) {
    var ctrl = this;

    ctrl.selected = $routeParams.username;
    ctrl.tenants = TenantResource.query();
    ctrl.tenants.$promise.then(
      function () {
        ctrl.getItems();
      },
      function (error) {
        alert(error.data.message);
      });


    ctrl.getItems = function () {
      if (ctrl.selected) {
        var tenantId = 0;
        angular.forEach(ctrl.tenants, function(value) {
          if (value.username === ctrl.selected) {
            tenantId = value.id;
          }
        });
        ctrl.subtenants = SubtenantResource.query({tenantId: tenantId});
        ctrl.subtenants.$promise.then(
          function () {
          },
          function (error) {
            alert(error.data.message);
          });
      }
    };

    ctrl.action = function (subtenant) {
      $log.debug(ctrl.actionKey);
      if (ctrl.actionKey === 'changeQuota') {
        var modalInstance = $uibModal.open({
          animation: false,
          templateUrl: 'subtenantChangeQuota.html',
          controller: 'SubChangeQuotaController as ctrl',
          resolve: {
            subtenant: function () {
              return subtenant;
            }
          }
        });

        modalInstance.result.then(function () {
          ctrl.getItems();
        });
      } else if (ctrl.actionKey === 'changePassword') {
        var modalInstance = $uibModal.open({
          animation: false,
          templateUrl: 'subtenantChangePassword.html',
          controller: 'SubChangePasswordController as ctrl',
          resolve: {
            subtenant: function () {
              return subtenant;
            }
          }
        });

        modalInstance.result.then(function () {
          ctrl.getItems();
        });
      } else if (ctrl.actionKey === 'enable') {
        subtenant.enabled = true;
        ctrl.promise = SubtenantResource.save(subtenant).$promise;
        ctrl.promise.then(function (response) {
          ctrl.getItems();
        }, function (response) {
          alert(response.data.message);
        });
      } else if (ctrl.actionKey === 'disable') {
        subtenant.enabled = false;
        ctrl.promise = SubtenantResource.save(subtenant).$promise;
        ctrl.promise.then(function (response) {
          ctrl.getItems();
        }, function (response) {
          alert(response.data.message);
        });
      }
      ctrl.actionKey = null;
    };
  }

  /** @ngInject */
  function SubChangeQuotaController($log, subtenant, $modalInstance, SubtenantResource) {
    var ctrl = this;

    ctrl.subtenant = subtenant;
    ctrl.quota = ctrl.subtenant.quota / 1024;

    ctrl.save = function () {
      ctrl.subtenant.quota = ctrl.quota * 1024;
      ctrl.promise = SubtenantResource.save(ctrl.subtenant).$promise;
      ctrl.promise.then(function (response) {
        $modalInstance.close();
      }, function (response) {
        alert(response.data.message);
      });

    };

    ctrl.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  }

  /** @ngInject */
  function SubChangePasswordController($log, subtenant, $modalInstance, SubtenantResource) {
    var ctrl = this;

    ctrl.subtenant = subtenant;

    ctrl.save = function () {
      ctrl.promise = SubtenantResource.save(ctrl.subtenant).$promise;
      ctrl.promise.then(function (response) {
        $modalInstance.close();
      }, function (response) {
        alert(response.data.message);
      });

    };

    ctrl.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  }
})();
