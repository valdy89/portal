/*globals alert */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('RepositoryHistoryController', RepositoryHistoryController)
    .controller('TenantHistoryController', TenantHistoryController);

  /** @ngInject */
  function RepositoryHistoryController($log, RepositoryResource) {
    var ctrl = this;

    ctrl.repositories = RepositoryResource.query();

    ctrl.repositories.$promise.then(
      function () {
        //$log.debug(ctrl.tenants);
      },
      function (error) {
        alert(error.data.message);
      });
  }

  /** @ngInject */
  function TenantHistoryController($log, TenantResource) {
    var ctrl = this;

    ctrl.selected = 'dursik12345';
    ctrl.tenants = TenantResource.query();
    ctrl.tenants.$promise.then(
      function () {
        ctrl.getItems();
      },
      function (error) {
        alert(error.data.message);
      });

    ctrl.repositoryLabels = ['1.1.2017', '1.2.2017', '1.3.2017', '1.4.2017', '1.6.2017', '1.7.2017', '23.7.2017'];
    ctrl.repositorySeries = ['Skutečná velikost GB', 'Zakoupená alokace GB', 'Počet VM', 'Počet Server', 'Počet Workstation'];

    ctrl.repositoryData = [
      [1, 3, 2, 3, 4, 4, 4],
      [3, 3, 4, 4, 6, 6, 6],
      [1, 1, 1, 2, 2, 2, 3],
      [2, 1, 3, 1, 1, 0, 3],
      [0, 0, 0, 1, 1, 1, 1]
    ];

    ctrl.creditLabels = ['1.1.2017', '1.2.2017', '1.3.2017', '1.4.2017', '1.6.2017', '1.7.2017', '23.7.2017'];
    ctrl.creditSeries = ['Kredit'];

    ctrl.creditData = [
      [100, 300, 200, 300, 100, 150, 50],
    ];


    ctrl.options = {
      legend: {display: true},
      scales: {
        yAxes: [{
          ticks: {
            beginAtZero: true
          }
        }]
      }
    };

  }

})();
