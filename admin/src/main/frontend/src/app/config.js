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
      $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';

      //routing
      $routeProvider
        .when('/', {
          templateUrl: 'app/dashboard/dashboard.html',
          controller: 'DashboardController as ctrl'
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
        .when('/repositoryHistory', {
          templateUrl: 'app/history/repositoryHistory.html',
          controller: 'RepositoryHistoryController as ctrl'
        })
        .when('/tenantHistory', {
          templateUrl: 'app/history/tenantHistory.html',
          controller: 'TenantHistoryController as ctrl'
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
