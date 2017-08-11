(function () {
  'use strict';

  angular.module('portal.resources', [])
    .factory('TenantResource', function ($log, $rootScope, $resource, EndpointConfigService) {
      return $resource(EndpointConfigService.getUrl('/tenant'));
    })
    .factory('InvoiceResource', function ($log, $rootScope, $resource, EndpointConfigService) {
        return $resource(EndpointConfigService.getUrl('/invoice'));
    })
    .factory('UserResource', function ($log, $rootScope, $resource, EndpointConfigService) {
      return $resource(EndpointConfigService.getUrl('/user'));
    })
    .factory('SubtenantResource', function ($log, $rootScope, $resource, EndpointConfigService) {
      return $resource(EndpointConfigService.getUrl('/subtenant'));
    });

})();
