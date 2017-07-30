(function () {
  'use strict';

  angular
    .module('portal')
    .value('cgBusyDefaults', {
      message: 'Čekejte prosím'
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
    .config(function ($logProvider, $routeProvider, $locationProvider, $httpProvider, uiSelectConfig, IdleProvider, KeepaliveProvider) {

      $logProvider.debugEnabled(true);
      uiSelectConfig.theme = 'bootstrap';
      $httpProvider.defaults.withCredentials = true;

      IdleProvider.idle(1700); // in seconds
      IdleProvider.timeout(100); // in seconds
      KeepaliveProvider.interval(60); // in seconds

      $httpProvider.interceptors.push('authInterceptor');

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
