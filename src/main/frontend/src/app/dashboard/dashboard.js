/*globals alert */
(function() {
'use strict';

angular
  .module('portal')
  .controller('DashboardController', DashboardController);

/** @ngInject */
function DashboardController($log, $rootScope, $http, EndpointConfigService) {
  var ctrl = this;

  $log.debug("user data: " + $rootScope.userData);
  ctrl.userData = $rootScope.userData;

  // ctrl.messagePromise = $http.get(EndpointConfigService.getUrl('/dashboard'));
  // ctrl.messagePromise.then(function (response) {
  //   ctrl.message = response.data;
  // }, function (response) {
  //   $log.debug(response);
  //   alert(response.data.message);
  // });
}

})();
