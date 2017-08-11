/*globals alert, document */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('SubtenantsController', SubtenantsController)
    .controller('CreateSubtenantController', CreateSubtenantController);

  /** @ngInject */
  function SubtenantsController($log, $rootScope, SubtenantResource, $mdDialog) {
    var ctrl = this;

    ctrl.getItems = function () {
      ctrl.subtenants = SubtenantResource.query();
      ctrl.subtenants.$promise.then(
        function () {

        },
        function (error) {
          alert(error.data.message);
        });
    };


    ctrl.createSubtenant = function () {
      var modalInstance = $mdDialog.show({
        //animation: false,
        templateUrl: 'createSubtenant.html',
        controller: 'CreateSubtenantController as ctrl',
        parent: angular.element(document.body),
        clickOutsideToClose: true,
        fullscreen: $rootScope.customFullscreen
      });

      modalInstance.finally(function () {
        ctrl.getItems();
      });
    };

    ctrl.getItems();

  }

  /** @ngInject */
  function CreateSubtenantController($log, $mdDialog, SubtenantResource) {
    var ctrl = this;

    ctrl.subtenant = {};
    ctrl.quota = 0;

    ctrl.save = function () {
      ctrl.subtenant.quota = ctrl.quota * 1024;
      ctrl.promise = SubtenantResource.save(ctrl.subtenant);
      ctrl.promise.$promise.then(
        function () {
          $mdDialog.hide('success');
        },
        function (error) {
          alert(error.data.message);
        });

    };

    ctrl.cancel = function () {
      $mdDialog.hide('cancel');
    };
  }

})();
