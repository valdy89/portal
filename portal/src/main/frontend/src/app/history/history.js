(function () {
  'use strict';

  angular
    .module('portal')
    .controller('TenantHistoryController', TenantHistoryController);

  /** @ngInject */
  function TenantHistoryController($log, $http, EndpointConfigService, ErrorHandlerService) {
    var ctrl = this;

    ctrl.dateFrom = new Date();
    ctrl.dateFrom.setMonth(ctrl.dateFrom.getMonth() - 2);
    ctrl.dateTo = new Date();

    ctrl.maxDate = new Date();
    ctrl.minDate = new Date();
    ctrl.minDate.setFullYear(ctrl.minDate.getFullYear() - 2);

    ctrl.recalculate = function () {
      var input = {from: ctrl.dateFrom, to: ctrl.dateTo};
      ctrl.promise = $http.post(EndpointConfigService.getUrl('/history'), input);
      ctrl.promise.then(function (response) {
        ctrl.repositoryLabels = response.data.repositoryLabels;
        ctrl.repositoryData = response.data.repositoryData;
        ctrl.creditLabels = response.data.creditLabels;
        ctrl.creditData = response.data.creditData;
        ctrl.repositorySeries = response.data.repositorySeries;
        ctrl.histories = response.data.histories;
      }, function (response) {
        ErrorHandlerService.handleError(response);
      });
    };

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
            stepSize: 1,
            callback: function (value) {
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

    ctrl.creditSeries = ['Kredit'];

    ctrl.creditOptions = {
      elements: {
        line: {
          tension: 0
        }
      },
      scales: {
        yAxes: [{
          ticks: {
            stepSize: 500
          }
        }]
      }
    };
    ctrl.recalculate();
  }

})();
