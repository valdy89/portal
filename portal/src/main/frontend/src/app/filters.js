(function () {
  'use strict';

  angular
    .module('portal')
    .filter('portalDateTime', function ($filter) {
      return function (date) {
        return $filter('date')(date, 'dd.MM.yyyy HH:mm:ss');
      };
    })
    .filter('portalDate', function ($filter) {
      return function (date) {
        return $filter('date')(date, 'dd.MM.yyyy');
      };
    });

})();
