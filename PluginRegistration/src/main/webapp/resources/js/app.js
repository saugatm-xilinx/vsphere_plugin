var sfApp = null;

(function (angular) {
	'use strict';
	sfApp = angular.module('sfApp', []);

	sfApp.service('sfservices', function ($http, $q) {
		var scope = this;
		var url = "/plugin-registration/";

		var response = {
			data: null
		};

		scope.postPlugin = function (data) {

			// create deferred object using $q
			var deferred = $q.defer(),
				urlpath = url + "plugin-service";

			// get posts form backend
			$http.post(urlpath, data).then(function (result) {

				console.log(result);
				// save fetched posts to the local variable
				response = result;
				// resolve the deferred
				deferred.resolve(response);
			}, function (error) {
				console.log(error);
				response.error = error;
				response.data = null;
				deferred.reject(response);
			});

			// set the posts object to be a promise until result comeback
			return deferred.promise;
		};

		scope.getPluginDefaultData = function () {

			// create deferred object using $q
			var deferred = $q.defer(),
				urlpath = url + "defaultData";

			// get posts form backend
			$http.get(urlpath).then(function (result) {

				console.log(result);
				// save fetched posts to the local variable
				response = result.data;
				// resolve the deferred
				deferred.resolve(response);
			}, function (error) {
				console.log(error);
				response.error = error;
				response.data = null;
				deferred.reject(response);
			});

			// set the posts object to be a promise until result comeback
			return deferred.promise;
		};

	});

	sfApp.controller("sfController", function ($scope, sfservices) {
		$scope.data = { url: "", port: 443, username: "", password: "" };
		$scope.hostInfo = {};
		$scope.alerInfo = { isvisible: false, title: "Error", message: "Unable to connect server.", class: "alert-danger" };

		$scope.registerPlugin = function () {

			if ($scope.isValide()) {
				var url = "https://" + $scope.data.url + ":" + $scope.data.port + "/sdk";
				$scope.alerInfo.isvisible = false;
				var modeldata = {
					connection: {
						url: url, port: $scope.data.port,
						username: $scope.data.username, password: $scope.data.password
					}, action: "register"
				};

				sfservices.postPlugin(modeldata).then(function (result) {
					console.log(result);
					$scope.isprocessing = false;
					
					$scope.alerInfo = {
						isvisible: true, title: "Success",
						message: "Plugin registration completed successfully on " + $scope.data.url, class: "alert-success"
					};
					//clear text boxes
					$scope.clear();

				}, function (e) {
					$scope.isprocessing = false;
					console.log(e.error);
					$scope.alerInfo = {
						isvisible: true, title: "Error",
						message: e.error.data.message, class: "alert-danger"
					};
				});
			}
			else {
				$scope.isprocessing = false;
			}
		};

		$scope.unregisterPlugin = function () {
			if ($scope.isValide()) {
				var url = "https://" + $scope.data.url + ":" + $scope.data.port + "/sdk";
				$scope.alerInfo.isvisible = false;
				var modeldata = {
					connection: {
						url: url, port: $scope.data.port,
						username: $scope.data.username, password: $scope.data.password
					}, action: "unregister"
				};
				sfservices.postPlugin(modeldata).then(function (result) {
					console.log(result);
					
					$scope.isprocessing = false;
					$scope.alerInfo = {
						isvisible: true, title: "Success",
						message: "Plugin unregistered successfully on " + $scope.data.url, class: "alert-success"
					};
					//clear text boxes.
					$scope.clear();
				}, function (e) {
					$scope.isprocessing = false;
					$scope.alerInfo = {
						isvisible: true, title: "Error",
						message: e.error.data.message, class: "alert-danger"
					};
				});
			}
			else {
				$scope.isprocessing = false;
			}
		};

		$scope.isValide = function () {
			$scope.isprocessing = true;
			if ($scope.data.url == "") {
				return false;
			}
			else if ($scope.data.port == "") {
				return false;
			}
			else if ($scope.data.username == "") {
				return false;
			}
			else if ($scope.data.password == "") {
				return false;
			}
			else {
				return true;
			}
		}

		$scope.getDefaultData = function () {
			sfservices.getPluginDefaultData().then(function (r) {
				console.log(r);
				$scope.hostInfo = {
					"email": r.email, "pluginUrl": r.pluginUrl, "name": r.name, "key": r.key,
					"summary": r.summary, "version": r.version, "company": r.company,
					"showInSolutionManager": r.showInSolutionManager, "serverThumbprint": r.serverThumbprint
				};
			});
		};

		$scope.getDefaultData();

		$scope.clear = function () {
			$scope.data = { url: "", port: 443, username: "", password: "" };
		};
		
		$scope.clearwithAlert = function () {
			$scope.data = { url: "", port: 443, username: "", password: "" };
			$scope.alerInfo.isvisible = false;
		};
		//sfservices.getPlugin();

	});

})(angular);