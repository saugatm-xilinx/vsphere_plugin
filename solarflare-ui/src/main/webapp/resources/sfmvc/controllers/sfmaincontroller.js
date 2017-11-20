(function (angular) {
    'use strict';

    sfApp.controller('sfmaincontroller', function ($scope, sfservices, uploadFile) {

        var vm = $scope;
        var fileUploadUrl = '';
        vm.selectedAdaptorFwList = [];
        vm.selectedAdaptorFw = [];
        vm.selectedAdaptorFw1 = {
            check: false
        };
        vm.treedata = [];
        vm.fwPath = {
            dest: ''
        };
        vm.fwFile = {
            upload: []
        };


        vm.fn = {
            hosts: {
                refresh: function (id) {
                    $scope.hostSelected(id);
                }
            },
            fw: {
                refresh: function (id) {
                    getAdaptorList(id);
                }
            }
        };

        // Fetches host list with children as adaptors and nic's
        vm.getHosts = function () {
            //debugger;
            sfservices.getHosts().then(function (result) {
                $scope.treedata = result.data;

            });
            if (devMode) {
                setTimeout(function () {
                    $scope.treedata = hosts;
                    $scope.$apply();
                }, 100);
            }
        };

        vm.showSelected = function (sel) {
            $scope.selected = sel;
            setTimeout(function () {
                vm.expandTree(sel.id);
            }, 200);
            //TODO: switch case for selection of right endpoint calls
            if (sel.type === 'HOST') {
                vm.hostSelected($scope.selected.id);
            } else if (sel.type === 'ADAPTER') {
                vm.adaptorSelected($scope.selected.id);
            }  else if (sel.type === 'NIC') {
                vm.nicSelected($scope.selected.id);
            }
        };

        vm.init = function () {
            $scope.getHosts();
        };

        vm.hostSelected = function (id) {
            // List of methods/services to invoke when a host is selected
            getHostDetails(id);
            getAdaptorList(id);
        };

        vm.adaptorSelected = function (id) {
            // List of methods/services to invoke when an adaptor is selected
            console.log('Selection of adaptor for id: '+ id);
        };

        vm.nicSelected = function (id) {
            // List of methods/services to invoke when an adaptor is selected
            console.log('Selection of nic for id: '+ id);
        };

        var getHostDetails = function (id) {
            sfservices.getHostDetails(id).then(function (result) {
                $scope.hostDetails = result.data;
            });
            if (devMode)
                $scope.hostDetails = hostDetails;
        };

        var getAdaptorList = function (id) {
            sfservices.getAdaptorList(id).then(function (result) {
                $scope.adaptorList = result.data;
            });
            if (devMode)
                $scope.adaptorList = adaptorList;
        };

        vm.adaptorSelectedFw = function (a) {
            if (vm.selectedAdaptorFwList.indexOf(a.id) === -1)
                vm.selectedAdaptorFwList.push(a.id);
            else
                vm.selectedAdaptorFwList.splice(vm.selectedAdaptorFwList.indexOf(a.id), 1);
            if (vm.selectedAdaptorFwList.length === 0) {
                vm.selectedAdaptorFw1.check = false;
                vm.selectedAdaptorFw = [];
            }
            if (vm.selectedAdaptorFwList.length === getUpdatableAdaptorsListCount()) {
                vm.selectedAdaptorFw1.check = true;
            }
            if (vm.selectedAdaptorFwList.length < getUpdatableAdaptorsListCount()) {
                vm.selectedAdaptorFw1.check = false;
            }
        };

        vm.selectAllAdaptorFwUpdate = function (adaptors) {
            if (vm.selectedAdaptorFwList.length !== 0 && vm.selectedAdaptorFw1.check === false) {
                vm.selectedAdaptorFwList = [];
                vm.selectedAdaptorFw = [];
                return;
            }
            adaptors.forEach(function (adaptor, i) {
                if (adaptor.laterVersionAvailable && vm.selectedAdaptorFwList.indexOf(adaptor.id)) {
                    vm.selectedAdaptorFwList.push(adaptor.id);
                    vm.selectedAdaptorFw[i] = true;
                }
            });
        };

        var getUpdatableAdaptorsListCount = function () {
            var c = 0;
            vm.adaptorList.forEach(function (adaptor, i) {
                if (adaptor.laterVersionAvailable)
                    c++;
            });
            return c;
        };

        vm.changeText = function (oFileInput, sTargetID) {
            document.getElementById(sTargetID).value = oFileInput[0].name;
        };

        vm.fileUpload = function () {
            if (vm.fwFile.upload && vm.fwFile.upload.length) {
                var fwFile = vm.fwFile.upload;
                uploadFile.uploadFileToUrl(fwFile, fileUploadUrl);
                //TODO :
                console.log('make a call to an endpoint with the file');
            } else {
                //TODO:
                console.log('make a call to an endpoint with the path of the fw file');
            }
            vm.clearAllFw();
            $("#closeModal").click();
        };

        vm.clearAllFw = function () {
            vm.fwFile.upload = [];
            vm.fwPath.dest = '';
            document.getElementById('txt').value = '';
            angular.element("input[type='file']").val(null);
        };

        vm.returnSelectedAdaptorsName = function () {
            var list = [];
            adaptorList.forEach(function (adaptor) {
                vm.selectedAdaptorFwList.forEach(function (t) {
                    if (t === adaptor.id) {
                        list.push(adaptor.name);
                    }
                });
            });
            return list.join(', ');
        };

        vm.expandTree = function (id) {
            var dropdownId ='dropdown-' + id;
            //document.getElementById(dropdownId).click();
            $("#"+dropdownId).click();
        };

        $scope.init();
    });
})(angular);
