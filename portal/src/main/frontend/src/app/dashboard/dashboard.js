/*globals alert, document */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('DashboardController', DashboardController)
    .controller('ChangeQuotaController', ChangeQuotaController)
    .controller('BuyCreditController', BuyCreditController);

  /** @ngInject */
  function DashboardController($log, $rootScope, TenantResource, $mdDialog) {
    var ctrl = this;

    $log.debug("user data: " + $rootScope.userData);
    ctrl.userData = $rootScope.userData;

    if (ctrl.userData) {
      ctrl.tenant = TenantResource.get();

      ctrl.tenant.$promise.then(
        function () {
          var pom = ctrl.tenant.quota - ctrl.tenant.usedQuota - 1;
          if (pom < 0) {
            pom = 0;
          }
          ctrl.repositoryData = [(pom / 1024).toFixed(1), (ctrl.tenant.usedQuota / 1024).toFixed(1)];
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
  function BuyCreditController($log, $mdDialog, $http, EndpointConfigService, UserResource) {
    var ctrl = this;

    ctrl.credit = 1000;
    ctrl.price = ctrl.credit / 10 * 3;
    ctrl.user = UserResource.get();
    ctrl.type = 0;


    ctrl.user.$promise.then(function (response) {
    }, function (response) {
      if (response.data) {
        alert(response.data.message);
      } else {
        alert("Server not responding, please try action again later.");
      }
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
        alert(response.data.message);
      });

    };

    ctrl.cancel = function () {
      $mdDialog.cancel();
    };
  }

  /** @ngInject */
  function ChangeQuotaController($log, $mdDialog, $http, tenant, EndpointConfigService) {
    var ctrl = this;

    ctrl.tenant = tenant;

    ctrl.quota = ctrl.tenant.quota / 1024;

    ctrl.save = function () {
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/tenant'), {quota: ctrl.quota * 1024});
      ctrl.promise.then(function () {
        $mdDialog.hide();
      }, function (response) {
        alert(response.data.message);
      });

    };

    ctrl.cancel = function () {
      $mdDialog.cancel();
    };
  }

})
();
