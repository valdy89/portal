(function () {
  'use strict';

  angular
    .module('portal')
    .controller('OrdersController', OrdersController);

  /** @ngInject */
  function OrdersController($log, $http, $mdDialog, InvoiceResource, ErrorHandlerService, EndpointConfigService) {
    var ctrl = this;

    ctrl.getItems = function() {
      ctrl.orders = InvoiceResource.query();

      ctrl.orders.$promise.then(
        function () {

        },
        function (response) {
          ErrorHandlerService.handleError(response);
        });
    };


    ctrl.delete = function (order) {
      ctrl.promise = $http.delete(EndpointConfigService.getUrl('/invoice/' + order.id));
      ctrl.promise.then(
        function () {
          ctrl.getItems();
          var alert = $mdDialog.alert();
          alert.title('Potvrzení zrušení')
            .textContent('Vaše objednávnka' + ('0000000' + order.id).slice(-8) + ' byla zrušena.')
            .theme('green')
            .ok('Zavřít');
          $mdDialog.show(alert);
        },
        function (response) {
          ErrorHandlerService.handleError(response);
        });
    };

    ctrl.getItems();
  }

})();
