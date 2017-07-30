(function () {
  'use strict';

  angular.module('portal.resources', [])
    .factory('TenantResource', function ($log, $rootScope, $resource, EndpointConfigService) {
      return $resource(EndpointConfigService.getUrl('/tenant'));
    });

})();
