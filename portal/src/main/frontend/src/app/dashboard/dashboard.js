/*globals document */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('DashboardController', DashboardController)
    .controller('ChangeQuotaController', ChangeQuotaController)
    .controller('BuyCreditController', BuyCreditController);

  /** @ngInject */
  function DashboardController($log, $rootScope, TenantResource, $mdDialog, ErrorHandlerService) {
    var ctrl = this;

    $log.debug("user data: " + $rootScope.userData);
    ctrl.userData = $rootScope.userData;


    ctrl.tenant = TenantResource.get();

    ctrl.tenant.$promise.then(
      function () {
        var pom = ctrl.tenant.quota - ctrl.tenant.usedQuota - 1;
        if (pom < 0) {
          pom = 0;
        }
        ctrl.repositoryData = [(pom / 1024).toFixed(1), (ctrl.tenant.usedQuota / 1024).toFixed(1)];
      },
      function (response) {
        ErrorHandlerService.handleError(response);
      });


    ctrl.repositoryLabels = ['Volný prostor', 'Využívaný prostor'];
    ctrl.options = {
      legend: {display: true}
    };

    ctrl.changeQuota = function () {
      var modalInstance = $mdDialog.show({
        //animation: false,
        templateUrl: 'changeQuota.html',
        controller: 'ChangeQuotaController as ctrl',
        parent: angular.element(document.body),
        clickOutsideToClose: true,
        fullscreen: $rootScope.customFullscreen,
        locals: {
          tenant: ctrl.tenant
        }
      });

      modalInstance.then(function () {
        ctrl.tenant = TenantResource.get();
      }, function () {
        $log.debug('cancel');
      });
    };

    ctrl.buyCredit = function () {
      var modalInstance = $mdDialog.show({
        // animation: false,
        templateUrl: 'buyCredit.html',
        controller: 'BuyCreditController as ctrl',
        parent: angular.element(document.body),
        clickOutsideToClose: true,
        fullscreen: $rootScope.customFullscreen
      });
      modalInstance.then(function () {
        ctrl.tenant = TenantResource.get();
      }, function () {
        $log.debug('cancel');
      });
    };
  }

  /** @ngInject */
  function BuyCreditController($log, $mdDialog, $http, EndpointConfigService, ErrorHandlerService, UserResource) {
    var ctrl = this;

    ctrl.credit = 1000;
    ctrl.price = ctrl.credit / 10 * 3;
    ctrl.user = UserResource.get();
    ctrl.type = 0;

    ctrl.user.$promise.then(function () {
    }, function (response) {
      ErrorHandlerService.handleError(response);
    });

    ctrl.recalculatePrice = function () {
      ctrl.price = Math.ceil(ctrl.credit / 10 * 3);
    };

    ctrl.recalculateCredit = function () {
      ctrl.credit = Math.floor(ctrl.price / 3 * 10);
    };

    ctrl.save = function () {
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/invoice'), {price: ctrl.price, type: ctrl.type, user: ctrl.user});
      ctrl.promise.then(function () {
        $mdDialog.hide();
      }, function (response) {
        ErrorHandlerService.handleError(response);
      });

    };

    ctrl.cancel = function () {
      $mdDialog.cancel();
    };
  }

  /** @ngInject */
  function ChangeQuotaController($log, $mdDialog, $http, tenant, EndpointConfigService, ErrorHandlerService) {
    var ctrl = this;

    ctrl.tenant = tenant;

    ctrl.quota = ctrl.tenant.quota / 1024;

    ctrl.save = function () {
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/tenant'), {quota: ctrl.quota * 1024});
      ctrl.promise.then(function () {
        $mdDialog.hide();
      }, function (response) {
        ErrorHandlerService.handleError(response);
      });

    };

    ctrl.cancel = function () {
      $mdDialog.cancel();
    };
  }

})
();
