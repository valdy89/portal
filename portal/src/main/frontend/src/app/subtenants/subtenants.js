/*globals document */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('SubtenantsController', SubtenantsController)
    .controller('CreateSubtenantController', CreateSubtenantController);

  /** @ngInject */
  function SubtenantsController($log, $rootScope, $mdDialog, SubtenantResource, ErrorHandlerService) {
    var ctrl = this;

    ctrl.getItems = function () {
      ctrl.subtenants = SubtenantResource.query();
      ctrl.subtenants.$promise.then(
        function () {

        },
        function (response) {
          ErrorHandlerService.handleError(response);
        });
    };


    ctrl.createSubtenant = function () {
      var subtenant = {};
      subtenant.enabled = true;
      var modalInstance = $mdDialog.show({
        //animation: false,
        templateUrl: 'createSubtenant.html',
        controller: 'CreateSubtenantController as ctrl',
        parent: angular.element(document.body),
        clickOutsideToClose: true,
        fullscreen: $rootScope.customFullscreen,
        locals: {
          subtenant: subtenant
        }
      });

      modalInstance.then(function () {
        ctrl.getItems();
      });
    };


    ctrl.updateSubtenant = function (subtenant) {

      var modalInstance = $mdDialog.show({
        //animation: false,
        templateUrl: 'updateSubtenant.html',
        controller: 'CreateSubtenantController as ctrl',
        parent: angular.element(document.body),
        clickOutsideToClose: true,
        fullscreen: $rootScope.customFullscreen,
        locals: {
          subtenant: subtenant,
          quota: subtenant.quota / 1024
        }
      });
      modalInstance.then(function () {
        ctrl.getItems();
      });
    };


    ctrl.deleteSubtenant = function (subtenant) {
      //console.log(subtenant);
      var modalInstance = $mdDialog.show({
        //animation: false,
        templateUrl: 'deleteSubtenant.html',
        controller: 'CreateSubtenantController as ctrl',
        parent: angular.element(document.body),
        clickOutsideToClose: true,
        fullscreen: $rootScope.customFullscreen,
        locals: {
          subtenant: subtenant,
        }
      });
      modalInstance.then(function () {
        ctrl.getItems();
      });
    };
    ctrl.getItems();

  }

  /** @ngInject */
  function CreateSubtenantController($log, $mdDialog, SubtenantResource, subtenant, $http, EndpointConfigService, ErrorHandlerService) {
    var ctrl = this;

    ctrl.subtenant = subtenant;
    if (subtenant.quota) {
      ctrl.quota = subtenant.quota / 1024;
    }

    ctrl.save = function () {

      ctrl.subtenant.quota = ctrl.quota * 1024;
      ctrl.promise = SubtenantResource.save(ctrl.subtenant);
      ctrl.promise.$promise.then(
        function () {
          $mdDialog.hide('success');
        },
        function (response) {
          ErrorHandlerService.handleError(response);
        });

    };

    ctrl.update = function () {
      ctrl.subtenant.quota = ctrl.quota * 1024;
      ctrl.promise = SubtenantResource.save(ctrl.subtenant);
      ctrl.promise.$promise.then(
        function () {
          $mdDialog.hide('success');
        },
        function (response) {
          ErrorHandlerService.handleError(response);
        });


    };
    ctrl.delete = function () {
      ctrl.promise = $http.delete(EndpointConfigService.getUrl('/subtenant/' + ctrl.subtenant.uid));
      ctrl.promise.then(
        function () {
          $mdDialog.hide('success');
        },
        function (response) {
          ErrorHandlerService.handleError(response);
        });


    };

    ctrl.cancel = function () {
      $mdDialog.cancel();
    };
  }

})();
