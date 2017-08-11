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
    })
    .filter('portalEnum', function ($log, ENUMS) {
      return function (input, enumName) {
        var ret = '';
        ENUMS[enumName].some(function (value) {
          if (value.id === input) {
            ret = value.name;
            return true;
          }
          if (value.id === '') {
            ret =  value.name;
          }
        });
        return ret;
      };
    });

})();
