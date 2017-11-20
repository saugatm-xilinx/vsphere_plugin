
(function (angular) {
    'use strict';

    sfApp.directive('treecontrol', ['$compile',
    function ($compile) {
        return {
            restrict: 'E',
            require: "treecontrol",
            replace: true,
            transclude: true,
            scope: {
                treeModel: "=",
                selectedNode: "=",
                nodeSearch:"=",
                onSelection: "&",
                nodeChildren: "@"
            },
            controller: function ($scope) {

                $scope.nodeChildren = $scope.nodeChildren
                    || 'children';
                $scope.expandedNodes = {};

                $scope.headClass = function (
                    node) {
                    if (node[$scope.nodeChildren]!=null && node[$scope.nodeChildren].length
                        && !$scope.expandedNodes[this.$id])
                        return "tree-collapsed";
                    else if (node[$scope.nodeChildren]!=null && node[$scope.nodeChildren].length
                        && $scope.expandedNodes[this.$id])
                        return "tree-expanded";
                    else
                        return "tree-normal"
                };

                $scope.arrowClass = function (node) {
                    if (node[$scope.nodeChildren]!=null && node[$scope.nodeChildren].length
                        && !$scope.expandedNodes[this.$id])
                        return "fa fa-caret-right";
                    else if (node[$scope.nodeChildren]!=null && node[$scope.nodeChildren].length
                        && $scope.expandedNodes[this.$id])
                        return "fa fa-caret-down";
                    else
                        return ""
                };

                $scope.nodeExpanded = function () {
                    return $scope.expandedNodes[this.$id];
                };

                $scope.selectNodeHead = function () {
                    $scope.expandedNodes[this.$id] = !$scope.expandedNodes[this.$id];
                };

                $scope.selectNodeLabel = function (
                    selectedNode) {
                    $scope.selectedScope = this.$id;
                    $scope.selectedNode = selectedNode;
                    if ($scope.onSelection)
                        $scope.onSelection({
                            node: selectedNode
                        });
                };

                $scope.selectedClass = function () {
                    return (this.$id == $scope.selectedScope) ? "tree-selected"
                        : "";
                };

                //tree template
                var template = '<ul>'
                    + '<li ng-repeat="node in node.'
                    + $scope.nodeChildren
                    + '|filter:nodeSearch" ng-class="headClass(node)">'
                    + '<i id="dropdown-{{node.id}}" class="tree-has-children" ng-class="arrowClass(node)" ng-click="selectNodeHead(node)"></i>'
                    + '<div class="tree-label" ng-class="selectedClass()" ng-click="selectNodeLabel(node)" tree-transclude></div>'
                    + '<treeitem ng-if="nodeExpanded()"></treeitem>'
                    + '</li>' + '</ul>';

                return {
                    templateRoot: $compile(template),
                    templateChild: $compile(template)
                }
            },
            compile: function (element, attrs,
                childTranscludeFn) {
                return function (scope, element,
                    attrs, treemodelCntr) {

                    function updateNodeOnRootScope(
                        newValue) {
                        if (angular
                            .isArray(newValue)) {
                            scope.node = {};
                            scope.node[scope.nodeChildren] = newValue;
                        } else {
                            scope.node = newValue;
                        }
                    }
                    scope
                        .$watch(
                        "treeModel",
                        updateNodeOnRootScope);
                    updateNodeOnRootScope(scope.treeModel);

                    //Rendering template for a root node
                    treemodelCntr
                        .templateRoot(
                        scope,
                        function (
                            clone) {
                            element
                                .html(
                                '')
                                .append(
                                clone);
                        });
                    // save the transclude function from compile (which is not bound to a scope as apposed to the one from link)
                    // we can fix this to work with the link transclude function with angular 1.2.6. as for angular 1.2.0 we need
                    // to keep using the compile function
                    scope.$treeTransclude = childTranscludeFn;
                }
            }
        };
    }])
    .directive("treeitem", function () {
        return {
            restrict: 'E',
            replace: true,
            require: "^treecontrol",
            link: function (scope, element, attrs,
                treemodelCntr) {

                // Rendering template for the current node
                treemodelCntr.templateChild(scope,
                    function (clone) {
                        element.html('').append(
                            clone);
                    });
            }
        }
    }).directive("treeTransclude", function () {
        return {
            link: function (scope, element, attrs, controller) {
                scope.$treeTransclude(scope, function (clone) {
                    element.empty();
                    element.append(clone);
                });
            }
        }
    }).directive("filesInput", function() {
        return {
            require: "ngModel",
            link: function postLink(scope,elem,attrs,ngModel) {
                elem.on("change", function(e) {
                    var files = elem[0].files;
                    ngModel.$setViewValue(files);
                })
            }
        }
    });
})(angular);

