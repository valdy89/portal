(function () {
  'use strict';

  var portalModule = angular.module('portal',
    [
      'portal.resources',
      'ngRoute',
      'ngSanitize',
      'ngResource',
      'ngCookies',
      'ui.select',
      'ui.bootstrap',
      'base64',
      'cgBusy'
    ]);


  portalModule.run(runBlock);

  /** @ngInject */
  function runBlock($log, $rootScope) {

    $rootScope.AlertController = function () {
      var ctrl = this;
      ctrl.alerts = [];

      ctrl.addAlert = function (msg) {
        ctrl.alerts.push({type: 'success', msg: msg});
      };

      ctrl.closeAlert = function (index) {
        ctrl.alerts.splice(index, 1);
      };
    };

    $log.debug('runBlock end');
  }

})();
