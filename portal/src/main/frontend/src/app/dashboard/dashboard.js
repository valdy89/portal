/*globals alert */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('DashboardController', DashboardController)
    .controller('ChangeQuotaController', ChangeQuotaController)
    .controller('CreateTenantController', CreateTenantController)
    .controller('BuyCreditController', BuyCreditController)
    .controller('ChangeTenantPasswordController', ChangeTenantPasswordController);

  /** @ngInject */
  function DashboardController($log, $rootScope, $uibModal, TenantResource,$mdDialog) {
    var ctrl = this;

    $log.debug("user data: " + $rootScope.userData);
    ctrl.userData = $rootScope.userData;

    if (ctrl.userData) {
      ctrl.tenant = TenantResource.get();

      ctrl.tenant.$promise.then(
        function () {
          $log.debug(ctrl.tenant);

          ctrl.repositoryData = [((ctrl.tenant.quota - ctrl.tenant.usedQuota - 1) / 1024).toFixed(1), (ctrl.tenant.usedQuota / 1024).toFixed(1)];
        },
        function (error) {
          alert(error.data.message);
        });
    }

    ctrl.repositoryLabels = ['Volný prostor', 'Využívany prostor'];
    ctrl.options = {
      legend: {display: true}
    };

    ctrl.changeQuota = function () {
      var modalInstance = $uibModal.open({
        animation: false,
        templateUrl: 'changeQuota.html',
        controller: 'ChangeQuotaController as ctrl',
        size: 'sm',
        resolve: {
          tenant: function () {
            return ctrl.tenant;
          }
        }
      });

      modalInstance.result.then(function () {
        ctrl.tenant = TenantResource.get();
      });
    };

    ctrl.changeTenantPassword = function () {
      var modalInstance = $uibModal.open({
        animation: false,
        templateUrl: 'changeTenantPassword.html',
        controller: 'ChangeTenantPasswordController as ctrl',
        size: 'sm'
      });
    };

    ctrl.createTenant = function () {
      var modalInstance = $mdDialog.show({
        animation: false,
        templateUrl: 'createTenant.html',
        controller: 'CreateTenantController as ctrl',
        parent: angular.element(document.body),

      clickOutsideToClose:true,
      fullscreen: $rootScope.customFullscreen
      });

    };

    ctrl.buyCredit = function () {
      var modalInstance = $mdDialog.show({
        // animation: false,
        templateUrl: 'buyCredit.html',
        controller: 'BuyCreditController as ctrl',
        parent: angular.element(document.body),

      clickOutsideToClose:true,
      fullscreen: $rootScope.customFullscreen
      });


    };
  }

  /** @ngInject */
  function BuyCreditController($log, $mdDialog, $http, EndpointConfigService) {
    var ctrl = this;

    ctrl.credit = 1000;
    ctrl.price = ctrl.credit / 10 * 3;

    ctrl.recalculatePrice = function () {
      ctrl.price = Math.ceil(ctrl.credit / 10 * 3);
    };

    ctrl.recalculateCredit = function () {
      ctrl.credit = Math.floor(ctrl.price / 3 * 10);
    };

    ctrl.save = function () {
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/invoice'), {price: ctrl.price});
      ctrl.promise.then(function (response) {
        $mdDialog.hide();
      }, function (response) {
        alert(response.data.message);
      });

    };

    ctrl.cancel = function () {
      $mdDialog.hide('cancel');
    };
  }

  /** @ngInject */
  function ChangeQuotaController($log, $uibModalInstance, $http, tenant, EndpointConfigService) {
    var ctrl = this;

    ctrl.tenant = tenant;
    ctrl.quota = ctrl.tenant.quota / 1024;

    ctrl.save = function () {
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/tenant'), {quota: ctrl.quota * 1024});
      ctrl.promise.then(function (response) {
        $uibModalInstance.close();
      }, function (response) {
        alert(response.data.message);
      });

    };

    ctrl.cancel = function () {
      $uibModalInstance.dismiss('cancel');
    };
  }

  /** @ngInject */
  function CreateTenantController($log, $mdDialog , $http, EndpointConfigService) {
    var ctrl = this;

    ctrl.quota = 0;

    ctrl.save = function () {
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/tenant'), {quota: ctrl.quota * 1024, password: ctrl.password});
      ctrl.promise.then(function (response) {
        $mdDialog.hide();
      }, function (response) {
        alert(response.data.message);
      });

    };

    ctrl.cancel = function () {
      $mdDialog.hide('cancel');
    };
  }

  /** @ngInject */
  function ChangeTenantPasswordController($log, $uibModalInstance, $http, EndpointConfigService) {
    var ctrl = this;

    ctrl.save = function () {
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/tenant'), {password: ctrl.password});
      ctrl.promise.then(function (response) {
        $uibModalInstance.close();
      }, function (response) {
        alert(response.data.message);
      });

    };

    ctrl.cancel = function () {
      $uibModalInstance.dismiss('cancel');
    };
  }

})
();
