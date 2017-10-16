/*globals document */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('DashboardController', DashboardController)
    .controller('ChangeQuotaController', ChangeQuotaController)
    .controller('BuyCreditController', BuyCreditController);

  /** @ngInject */
  function DashboardController($log, $routeParams, $rootScope, TenantResource, $mdDialog, ErrorHandlerService) {
    var ctrl = this;

    if ($routeParams.payment) {
      var alert = $mdDialog.alert();
      if ($routeParams.payment === 'success') {
        alert.title('Potvrzení platby')
          .textContent('Vaše platba byla úspěšně provedena. Faktura bude odeslaná na váš email.')
          .theme('green')
          .ok('Zavřít');
      } else {
        alert.title('Chyba')
          .theme('red')
          .textContent('Vaše platba byla zamítnuta vydavatelem karty.')
          .ok('Zavřít');
      }
      $mdDialog.show(alert);
    }

    ctrl.tenant = TenantResource.get();

    ctrl.tenant.$promise.then(
      function () {
        var pom = ctrl.tenant.quota - ctrl.tenant.usedQuota - 1;
        if (pom < 0) {
          pom = 0;
        }
        ctrl.repositoryData = [(pom / 1024).toFixed(1), (ctrl.tenant.usedQuota / 1024).toFixed(1)];
        ctrl.tenant.disabled = !ctrl.tenant.enabled;
      },
      function (response) {
        ErrorHandlerService.handleError(response);
      });


    ctrl.repositoryLabels = ['Volný prostor', 'Využívaný prostor'];
    ctrl.options = {
      legend: {display: true},
      tooltips: {
        callbacks: {
          label: function (tooltipItem, data) {
            $log.debug(data);
            var dataset = data.datasets[tooltipItem.datasetIndex];
            var currentValue = dataset.data[tooltipItem.index];
            return data.labels[tooltipItem.index] + ': ' + currentValue + ' GB';
          }
        }
      }
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
  function BuyCreditController($log, $mdDialog, $window, $http, EndpointConfigService, ErrorHandlerService, UserResource) {
    var ctrl = this;

    ctrl.credit = 1000;
    ctrl.price = ctrl.credit / 10 * 2.5;
    ctrl.user = UserResource.get();
    ctrl.type = '0';
    ctrl.buttonLabel = 'Zaplatit';

    ctrl.user.$promise.then(function () {
    }, function (response) {
      ErrorHandlerService.handleError(response);
    });

    ctrl.recalculatePrice = function () {
      ctrl.price = Math.ceil(ctrl.credit / 10 * 2.5);
    };

    ctrl.recalculateCredit = function () {
      ctrl.credit = Math.floor(ctrl.price / 2.5 * 10);
    };

    ctrl.changeButtonLabel = function () {
      ctrl.buttonLabel = (ctrl.type === '0' ? 'Zaplatit' : 'Objednat');
    };

    ctrl.save = function () {
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/payment'), {price: ctrl.price, type: ctrl.type, user: ctrl.user});
      ctrl.promise.then(function (response) {
        if (ctrl.type === '0') {
          $window.location.href = response.data.bankUrl;
        } else {
          $mdDialog.hide();
        }
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
