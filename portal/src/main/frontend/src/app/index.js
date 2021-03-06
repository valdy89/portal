(function () {
  'use strict';

  var portalModule = angular.module('portal',
    [
      'portal.resources',
      'ngRoute',
      'ngSanitize',
      'ngResource',
      'ngCookies',
      'base64',
      'cgBusy',
      'chart.js',
      'vcRecaptcha',
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
      {id: 'Unpaid', name: 'Neuhrazená'},
      {id: 'Paid', name: 'Uhrazená'},
      {id: 'PartialPaid', name: 'Uhrazená částečně'},
      {id: 'Overpaid', name: 'Uhrazená'},
      {id: 'Canceled', name: 'Zrušena'}
    ]
  });

  portalModule.run(runBlock);

  /** @ngInject */
  function runBlock($log, $rootScope, $locale) {

    $locale.NUMBER_FORMATS.GROUP_SEP = '';

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
