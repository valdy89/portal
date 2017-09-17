(function () {
  'use strict';

  angular
    .module('portal')
    .value('cgBusyDefaults', {
      message: 'Čekejte prosím'
    })
    .service('authInterceptor', function($q, $log, $location, $rootScope) {
      var service = this;

      service.responseError = function(response) {
        if(response.status === 401 || response.status === 403) {
          $rootScope.$broadcast('userLoggedOut', null);
          $location.path('/login');
        }

        return $q.reject(response);
      };
    })
    .config(function ($logProvider, $routeProvider, $locationProvider, $httpProvider, uiSelectConfig) {

      $logProvider.debugEnabled(true);
      uiSelectConfig.theme = 'bootstrap';

      $httpProvider.defaults.withCredentials = true;
      $httpProvider.interceptors.push('authInterceptor');
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
        .when('/settings', {
          templateUrl: 'app/settings/settings.html',
          controller: 'SettingsController as ctrl'
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
