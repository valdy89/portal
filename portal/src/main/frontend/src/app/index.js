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
      'cgBusy',
      'chart.js',
      'vcRecaptcha',
      'ngIdle',
      'ngMaterial',
      'md.data.table'
    ]).config(function ($mdThemingProvider) {
    $mdThemingProvider.theme('default')
      .primaryPalette('blue-grey')
      .accentPalette('amber');
  });

  portalModule.value('ENUMS', {
    'PAYMENT_STATUS': [
      {id: '', name: 'Založená'},
      {id: 'Unpaid', name: 'Neuhrazena'},
      {id: 'Paid', name: 'Zaplacena'},
      {id: 'PartialPaid', name: 'Zaplacena částečně'},
      {id: 'Overpaid', name: 'Zaplacena'}
    ]
  });

  portalModule.run(runBlock);

  /** @ngInject */
  function runBlock($log, $rootScope, Idle) {

    Idle.watch();

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
