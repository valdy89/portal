/*globals moment */
(function () {
  'use strict';

  angular
    .module('portal')
    .value('cgBusyDefaults', {
      message: 'Čekejte prosím',
      delay: 500
    })
    .service('authInterceptor', function($q, $log, $location) {
      var service = this;

      service.responseError = function(response) {
        if(response.status === 401 || response.status === 403) {
          $location.path('/login');
        }

        return $q.reject(response);
      };
    })
    .service('ErrorHandlerService', function ($location, $mdDialog) {
      var service = this;

      service.handleError = function(response) {
        if(response.status === 401 || response.status === 403) {
          $location.path('/login');
          return;
        }

        var message = '';
        if (response.data) {
          message = response.data.message;
        } else {
          message = "Došlo k chybě při komunikaci se servrem, zkuste akci později";
        }
        var alert = $mdDialog.alert().title('Chyba').textContent(message).ok('Zavřít');
        $mdDialog.show(alert);
      };
    })
    .config(function ($logProvider, $routeProvider, $mdDateLocaleProvider, $locationProvider, $httpProvider, uiSelectConfig) {

      $logProvider.debugEnabled(true);
      uiSelectConfig.theme = 'bootstrap';

      $httpProvider.defaults.withCredentials = true;
      //$httpProvider.interceptors.push('authInterceptor');
      $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';

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
