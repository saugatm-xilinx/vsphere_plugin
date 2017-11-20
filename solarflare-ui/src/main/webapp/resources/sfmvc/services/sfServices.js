(function (angular) {
    'use strict';

    sfApp.service('sfservices', function ($http, $q) {
        var scope = this;

        var response = {
            data: null
        };
        scope.getHosts = function () {

            // create deferred object using $q
            var deferred = $q.defer(), url = com_msys_solarflare.webContextPath + "/rest/services/hosts/";

            // get posts form backend
            $http.get(url).then(function (result) {

                //console.log(result.data);
                // save fetched posts to the local variable
                response.data = result.data;
                // resolve the deferred
                deferred.resolve(response);
            }, function (error) {
                response.error = error;
                response.data = null;
                deferred.reject(response);
            });

            // set the posts object to be a promise until result comeback
            return deferred.promise;

        };

        scope.getHostDetails = function (id) {

            // create deferred object using $q
            var deferred = $q.defer(), url = com_msys_solarflare.webContextPath + "/rest/services/hosts/" + id;

            // get posts form backend
            $http.get(url).then(function (result) {

                //console.log(result.data);
                // save fetched posts to the local variable
                response.data = result.data;
                // resolve the deferred
                deferred.resolve(response);
            }, function (error) {
                response.error = error;
                response.data = null;
                deferred.reject(response);
            });

            // set the posts object to be a promise until result comeback
            return deferred.promise;

        };

        scope.getAdaptorList = function (id) {

            // create deferred object using $q
            var deferred = $q.defer(),
                url = com_msys_solarflare.webContextPath + "/rest/services/hosts/" + id + "/adapters/";

            // get posts form backend
            $http.get(url).then(function (result) {

                //console.log(result.data);
                // save fetched posts to the local variable
                response.data = result.data;
                // resolve the deferred
                deferred.resolve(response);
            }, function (error) {
                response.error = error;
                response.data = null;
                deferred.reject(response);
            });

            // set the posts object to be a promise until result comeback
            return deferred.promise;

        };

    });

    sfApp.service('uploadFile', ['$http', function ($http) {
        this.uploadFileToUrl = function (file, uploadUrl) {
            var fd = new FormData();
            fd.append('file', file);
            $http.post(uploadUrl, fd, {
                transformRequest: angular.identity,
                headers: {'Content-Type': undefined}
            })
            .success(function () {

            })
            .error(function () {

            });
        }
    }]);

})(angular);
