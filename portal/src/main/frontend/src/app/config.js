/*globals window, moment */
(function () {
  'use strict';

  angular
    .module('portal')
    .value('cgBusyDefaults', {
      message: 'Čekejte prosím',
      delay: 500
    })
    .service('authInterceptor', function($q, $log) {
      var service = this;

      service.responseError = function(response) {
        $log.debug(response);
        if (response.status === 401 || response.status === -1){
          window.location = "/login";
        }
        return $q.reject(response);
      };
    })
    .config(function ($logProvider, $routeProvider, $mdDateLocaleProvider, $locationProvider, $httpProvider, uiSelectConfig, IdleProvider, KeepaliveProvider) {

      $logProvider.debugEnabled(true);
      uiSelectConfig.theme = 'bootstrap';
      $httpProvider.defaults.withCredentials = true;

      IdleProvider.idle(1700); // in seconds
      IdleProvider.timeout(100); // in seconds
      KeepaliveProvider.interval(60); // in seconds

      $httpProvider.interceptors.push('authInterceptor');

      $mdDateLocaleProvider.months = ['Leden', 'Únor', 'Březen', 'Duben', 'Květen', 'Červen', 'Červenec', 'Srpen', 'Září', 'Říjen', 'Listopad', 'Prosinec'];
      $mdDateLocaleProvider.shortMonths = ['Led','Úno','Bře','Dub','Kvě','Čvn','Čvc','Srp','Zář','Říj','Lis','Pro'];
      $mdDateLocaleProvider.days = ['Pondělí', 'Úterý', 'Středa', 'Čtvrtek', 'Pátek', 'Sobota', 'Neděle'];
      $mdDateLocaleProvider.shortDays = ['Ne', 'Po', 'Út', 'St', 'Čt', 'Pá', 'So'];
      $mdDateLocaleProvider.firstDayOfWeek = 1;
      $mdDateLocaleProvider.parseDate = function(dateString) {
        var m = moment(dateString, 'DD.MM.YYYY', true);
        return m.isValid() ? m.toDate() : new Date(NaN);
      };

      $mdDateLocaleProvider.formatDate = function(date) {
        var m = moment(date);
        return m.isValid() ? m.format('DD.MM.YYYY') : '';
      };

      //routing
      $routeProvider
        .when('/', {
          templateUrl: 'app/dashboard/dashboard.html',
          controller: 'DashboardController as ctrl'
        })
        .when('/login', {
          templateUrl: 'app/login/login.html',
          controller: 'LoginController as ctrl'
        })
        .when('/verify', {
          templateUrl: 'app/login/changePassword.html',
          controller: 'ChangePasswordController as ctrl'
        })
        .when('/subtenants', {
          templateUrl: 'app/subtenants/subtenants.html',
          controller: 'SubtenantsController as ctrl'
        })
        .when('/history', {
          templateUrl: 'app/history/history.html',
          controller: 'TenantHistoryController as ctrl'
        })
        .when('/orders', {
          templateUrl: 'app/orders/orders.html',
          controller: 'OrdersController as ctrl'
        })
        .otherwise({
          redirectTo: '/'
        });

      $locationProvider.html5Mode({
        enabled: true,
        requireBase: true
      });
    });

})();
