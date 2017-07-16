(function () {
  'use strict';

  angular
    .module('portal')
    .value('cgBusyDefaults', {
      message: 'Čekejte prosím'
    })
    .config(function ($logProvider, $routeProvider, $locationProvider, $httpProvider, uiSelectConfig) {

      $logProvider.debugEnabled(true);
      uiSelectConfig.theme = 'bootstrap';
      $httpProvider.defaults.withCredentials = true;

      //routing
      $routeProvider
        .when('/', {
          templateUrl: 'app/dashboard/dashboard.html',
          controller: 'DashboardController as ctrl',
        })
        .when('/login', {
          templateUrl: 'app/login/login.html',
          controller: 'LoginController as login'
        })
        .when('/tenants', {
          templateUrl: 'app/tenant/tenants.html',
          controller: 'TenantsController as ctrl'
        })
        .when('/subtenants', {
          templateUrl: 'app/tenant/subtenants.html',
          controller: 'SubtenantsController as ctrl'
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
