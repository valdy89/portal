/*globals alert */
(function() {
    'use strict';

    angular
      .module('portal')
      .controller('SubtenantsController', SubtenantsController)
      .controller('CreateSubtenantController', SubtenantsController);

    /** @ngInject */
    function SubtenantsController($log, $rootScope, TenantResource, $mdDialog) {
      var ctrl = this;
      $log.debug("user data: " + $rootScope.userData);
      ctrl.userData = $rootScope.userData;

      if (ctrl.userData) {
        ctrl.tenant = TenantResource.get();
        ctrl.tenant.$promise.then(
          function() {
            var pom = ctrl.tenant.quota - ctrl.tenant.usedQuota - 1;
            if (pom < 0) {
              pom = 0;
            }
            ctrl.repositoryData = [(pom / 1024).toFixed(1), (ctrl.tenant.usedQuota / 1024).toFixed(1)];
          },
          function(error) {
            alert(error.data.message);
          });
      }

      ctrl.createSubtenant = function() {
        var modalInstance = $mdDialog.show({
          //animation: false,
          templateUrl: 'createSubtenant.html',
          controller: 'CreateSubtenantController as ctrl',
          parent: angular.element(document.body),
          clickOutsideToClose: true,
          fullscreen: $rootScope.customFullscreen,
          locals: {
            tenant: ctrl.tenant
          }
        });

        modalInstance.finally(function() {
          ctrl.tenant = TenantResource.get();
        });
      };



      }


      function CreateSubtenantController($log, $mdDialog, $http, tenant, EndpointConfigService) {
        var ctrl = this;

        ctrl.tenant = tenant;

        ctrl.subtenant.quota = ctrl.tenant.quota / 1024;

        ctrl.save = function() {
          //todo: call backend
          // ctrl.promise = $http.post(EndpointConfigService.getUrl('/tenant'), {quota: ctrl.quota * 1024});
          // ctrl.promise.then(function (response) {
          //   $mdDialog.hide();
          // }, function (response) {
          //   alert(response.data.message);
          // });

        };

        ctrl.cancel = function() {
          $mdDialog.hide('cancel');
        };
      }

    })();
