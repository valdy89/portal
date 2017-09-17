/*globals alert */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('SettingsController', SettingsController);


  /** @ngInject */
  function SettingsController($log, $location, $http, EndpointConfigService) {
    var ctrl = this;

    ctrl.configs = {};
    ctrl.secure = {};

      ctrl.getItems = function () {
      ctrl.mypromise = $http.get(EndpointConfigService.getUrl('/settings'));
      ctrl.mypromise.then(function (response) {
        ctrl.configs = response.data;
        $log.debug(ctrl.configs);
      });
    };

    ctrl.save = function () {
      var values = {};
      angular.extend(values, ctrl.configs);
      angular.extend(values, ctrl.secure);

      ctrl.promise = $http.post(EndpointConfigService.getUrl('/settings'), values);
      ctrl.promise.then(function () {
        ctrl.getItems();
        ctrl.secure = {};
        alert("Nastavení uloženo.");
      }, function (response) {
        $log.debug(response);
        if (response.data) {
          alert(response.data.message);
        } else {
          alert("Server not responding, please try action again later.");
        }
      });
    };

    ctrl.getItems();
  }
})();
