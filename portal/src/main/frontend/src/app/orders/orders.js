(function () {
  'use strict';

  angular
    .module('portal')
    .controller('OrdersController', OrdersController);

  /** @ngInject */
  function OrdersController($log, InvoiceResource, ErrorHandlerService) {
    var ctrl = this;

    ctrl.orders = InvoiceResource.query();

    ctrl.orders.$promise.then(
      function () {
      },
      function (response) {
        ErrorHandlerService.handleError(response);
      });
  }

})();
