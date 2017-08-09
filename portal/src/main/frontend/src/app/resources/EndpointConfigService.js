/*globals _contextPath */
(function () {
  'use strict';

  angular.module('portal.resources')
    .constant('CURRENT_BACKEND', 'LOCALHOST')
    .service('EndpointConfigService', EndpointConfigService);

  /** @ngInject */
  function EndpointConfigService($location, CURRENT_BACKEND) {
    // console.log(CURRENT_BACKEND);
    var service = this,
      endpointMap = {
        LOCALHOST: {URI: 'http://localhost:8080', contextPath: '', root: '/api', format: ''},
        REMOTE: {URI: $location.protocol() + '://' + $location.host() + ':' + $location.port(), contextPath: _contextPath , root: '/api', format: ''}
      },
      currentEndpoint = endpointMap[CURRENT_BACKEND];

    service.getUrl = function (model) {
      return currentEndpoint.URI + currentEndpoint.contextPath + currentEndpoint.root + model;
    };

    service.getAppUrl = function (model) {
      return currentEndpoint.URI + currentEndpoint.contextPath + model;
    };
  }

})();
