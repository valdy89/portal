/*globals alert */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('OrdersController', OrdersController);

  /** @ngInject */
  function OrdersController($log, InvoiceResource) {
    var ctrl = this;

    ctrl.orders = InvoiceResource.query();

    ctrl.orders.$promise.then(
      function () {
      },
      function (error) {
        alert(error.data.message);
      });
  }

})();
