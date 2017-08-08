/*globals alert */
(function () {
  'use strict';

  angular
    .module('portal')
    .controller('TenantHistoryController', TenantHistoryController);

  /** @ngInject */
  function TenantHistoryController($log) {
    var ctrl = this;

    ctrl.repositoryLabels = ['1.1.2017', '1.2.2017', '1.3.2017', '1.4.2017', '1.6.2017', '1.7.2017'];
    ctrl.repositorySeries = ['Skutečná velikost GB', 'Zakoupená alokace GB', 'Počet VM', 'Počet Server', 'Počet Workstation'];

    ctrl.repositoryData = [
      [20, 50, 100, 75, 55, 100],
      [80, 50, 0, 75, 60, 50],
      [1, 1, 0, 1, 2, 2, 1],
      [0, 1, 1, 2, 2, 0, 2],
      [1, 1, 0, 1, 2, 2, 1]

    ];
    ctrl.repositoryOverride =
      [{
        yAxisID: 'left-y-axis'
      }, {
        yAxisID: 'left-y-axis'
      }, {
        yAxisID: 'right-y-axis'
      }, {
        yAxisID: 'right-y-axis'
      }, {
        yAxisID: 'right-y-axis'
      }];

    ctrl.repositoryOptions = {
      legend: {display: true},
      scales: {
        yAxes: [{
          id: 'left-y-axis',
          type: 'linear',
          position: 'left',
          ticks: {
            beginAtZero: true,
            callback: function (value, index, values) {
              return value + 'GB';
            }
          }
        }, {
          id: 'right-y-axis',
          type: 'linear',
          position: 'right',
          ticks: {
            beginAtZero: true,
            stepSize: 1
          }
        }]
      }
    };

    ctrl.creditLabels = ['1.1.2017', '1.2.2017', '1.3.2017', '1.4.2017', '1.6.2017', '1.7.2017', '23.7.2017'];
    ctrl.creditSeries = ['Kredit'];

    ctrl.creditData = [
      [100, 300, 200, 300, 100, 150, 50]
    ];

  }

})();
