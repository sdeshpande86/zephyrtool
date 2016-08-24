(function() {
	'use strict';
	// App Module: the name AngularStore matches the ng-app attribute in the
	// main <html> tag
	// the route provides parses the URL and injects the appropriate partial
	// page
	angular.module('app', [ 'ngRoute', 'ngCookies' ]).config(config).run(run).directive(
			'ngDropdownMultiselect', ngDropdownMultiselect).service('sharedProperties',sharedProperties);
	config.$inject = [ '$routeProvider', '$locationProvider' ];
	function config($routeProvider, $locationProvider) {

		$routeProvider.when('/tree', {
			controller : 'TreeController',
			templateUrl : 'tree/tree.view.html',
		}).otherwise({
			redirectTo : '/'
		});
	}

	run.$inject = [ '$rootScope', '$route', '$location', '$cookieStore',
	                '$http','sharedProperties' ];
	function run($rootScope, $route, $location, $cookieStore, $http,sharedProperties) {

		delete $rootScope.flash;
		getUsecases($rootScope, $http, $location);
		$rootScope.testTypes = [ {
			id : "functionality",
			label : "functionality"
		}, {
			id : "scalability",
			label : "scalability"
		}, {
			id : "performance",
			label : "performance"
		}, {
			id : "debuggability",
			label : "debuggability"
		}, {
			id : "limits",
			label : "limits"
		}, {
			id : "upgradability",
			label : "upgradability"
		}, {
			id : "backward compatibility",
			label : "backward compatibility"
		}, {
			id : "security",
			label : "security"
		}, {
			id : "ui",
			label : "ui"
		}, {
			id : "negative case",
			label : "negative case"
		} ];
		$rootScope.testType = {
			ids : {
				"functionality" : false
			}
		};

		$rootScope.components = [ {
			id : "apm-apache",
			label : "apm-apache"
		}, {
			id : "apm-core",
			label : "apm-core"
                }, {
                        id : "apm-cpp",
                        label : "apm-cpp"
		}, {
			id : "apm-db",
			label : "apm-db"
		}, {
			id : "apm-dotnet",
			label : "apm-dotnet"
		}, {
			id : "apm-java",
			label : "apm-java"
                }, {
                        id : "apm-nodejs",
                        label : "apm-nodejs"
                }, {
                        id : "apm-php",
                        label : "apm-php"
                }, {
                        id : "apm-python",
                        label : "apm-python"
                }, {
                        id : "apm-ruby",
                        label : "apm-ruby"
                }, {
                        id : "apm-unified-agent",
                        label : "apm-unified-agent"
                }, {
        		id : "analytics",
        		label : "analytics"
		}, {
        		id : "eum-mobile",
        		label : "eum-mobile"
		}, {
        		id : "eum-platform",
   		     	label : "eum-platform"
		}, {
        		id : "eum-synthetic",
        		label : "eum-synthetic"
		}, {
        		id : "eum-web",
        		label : "eum-web"
		}, {
        		id : "event-service",
        		label : "event-service"
		}, {
        		id : "npm",
        		label : "npm"
		}, {
                        id : "platform-deployment",
                        label : "platform-deployment"
                }, {
                        id : "platform-eventdata",
                        label : "platform-eventdata"
                }, {
                        id : "platform-metricdata",
                        label : "platform-metricdata"
		}, {
        		id : "platform-services-alerting",
        		label : "platform-services-alerting"
		}, {
        		id : "platform-services-dashboards",
        		label : "platform-services-dashboards"
		}, {
        		id : "platform-services-infrastructure",
		        label : "platform-services-infrastructure"
		}, {
        		id : "platform-services-metadata",
        		label : "platform-services-metadata"
		}, {
        		id : "platform-services-reports",
        		label : "platform-services-reports"
		}, {
        		id : "sim",
        		label : "sim"
		}, {
			id : "ui-platform",
			label : "ui-platform"
		} ];

		$rootScope.component = {
			ids : {
				"apm-apache" : false
			}
		};
		$rootScope.update = function() {
			$rootScope.sidebarHide = true;
			$rootScope.isfilter = false;
			sharedProperties.setValue("sharedUseCase",$rootScope.selectedUseCase)
			console.log(sharedProperties.getValue("sharedUseCase"));
			$route.reload()
			$location.path('/tree');
		}

		$rootScope.filter = function() {
			$rootScope.isfilter = true;
			$route.reload()
			$location.path('/tree');
		}

		$rootScope.clearFilter = function() {
			$rootScope.testType = {
				ids : {
					"functionality" : false
				}
			};

			$rootScope.component = {
				ids : {
					"apm-apache" : false
				}
			};
			$rootScope.isfilter = false;
			$route.reload()
			$location.path('/tree');
		}
	}

	function sharedProperties() {
		var hashtable = {};
		
		return {
			setValue: function (key, value) {
				hashtable[key] = value;
			},
			getValue: function (key) {
				return hashtable[key];
			}
		};
	}
	
	function getUsecases($rootScope, $http, $location) {
		$http(
				{
					method : "GET",
					url : $location.protocol() + '://' + $location.host() + ':'
							+ $location.port() + '/zephyrtool/rest/getusecases'
				}).then(
				function mySucces(response) {
					$rootScope.usecases = response.data;
				},
				function myError(response) {
					$rootScope.flash = {
						message : "Failed to get usecases with a status code"
								+ response.statusText,
						type : true
					};
				});

	}

	ngDropdownMultiselect.inject = [ '$filter', '$document', '$compile',
			'$parse', '$route', '$location' ];
	function ngDropdownMultiselect($filter, $document, $compile, $parse,
			$route, $location) {
		return {
			restrict : 'AE',
			scope : {
				model : '=',
				options : '=',
				extraSettings : '=',
				events : '=',
				searchFilter : '=?',
				translationTexts : '=',
				groupBy : '@'
			},
			template : function(element, attrs) {
				var checkboxes = attrs.checkboxes ? true : false;
				var groups = attrs.groupBy ? true : false;

				var template = '<div class="multiselect-parent btn-group dropdown-multiselect">';
				template += '<button type="button" class="dropdown-toggle" ng-class="settings.buttonClasses" style="overflow:hidden; white-space:nowrap; text-overflow:ellipsis;text-align:left;" ng-click="toggleDropdown()">{{getButtonText()}}&nbsp;<span class="caret"></span></button>';
				template += '<ul class="dropdown-menu dropdown-menu-form" ng-style="{display: open ? \'block\' : \'none\', height : settings.scrollable ? settings.scrollableHeight : \'auto\' }">';
				template += '<li role="presentation" ng-repeat="option in options | filter: searchFilter">';
				template += '<a role="menuitem" tabindex="-1" ng-click="setSelectedItem(getPropertyForObject(option,settings.idProp))">';
				template += '<span data-ng-class="{\'glyphicon glyphicon-ok\': isChecked(getPropertyForObject(option,settings.idProp))}"></span> {{getPropertyForObject(option, settings.displayProp)}}</a>';
				template += '</li>';
				template += '</ul>';
				template += '</div>';

				element.html(template);
			},
			link : function($scope, $element, $attrs) {
				var $dropdownTrigger = $element.children()[0];

				$scope.toggleDropdown = function() {
					$scope.open = !$scope.open;
				};

				$scope.checkboxClick = function($event, id) {
					$scope.setSelectedItem(id);
					$event.stopImmediatePropagation();
				};

				$scope.externalEvents = {
					onItemSelect : angular.noop,
					onItemDeselect : angular.noop,
					onInitDone : angular.noop,
					onMaxSelectionReached : angular.noop
				};

				$scope.settings = {
					dynamicTitle : true,
					scrollable : false,
					scrollableHeight : '300px',
					closeOnBlur : true,
					displayProp : 'label',
					idProp : 'id',
					externalIdProp : 'id',
					enableSearch : false,
					selectionLimit : 0,
					closeOnSelect : false,
					buttonClasses : 'btn btn-default',
					closeOnDeselect : false,
					groupByTextProvider : null,
					smartButtonTextConverter : angular.noop
				};

				$scope.texts = {
					selectionOf : '/',
					buttonDefaultText : 'Select Value(s)',
					dynamicButtonTextSuffix : 'checked'
				};

				$scope.searchFilter = $scope.searchFilter || '';

				if (angular.isDefined($scope.settings.groupBy)) {
					$scope.$watch('options', function(newValue) {
						if (angular.isDefined(newValue)) {
							$scope.orderedItems = $filter('orderBy')(newValue,
									$scope.settings.groupBy);
						}
					});
				}

				angular.extend($scope.settings, $scope.extraSettings || []);
				angular.extend($scope.externalEvents, $scope.events || []);
				angular.extend($scope.texts, $scope.translationTexts);

				$scope.singleSelection = $scope.settings.selectionLimit === 1;

				function getFindObj(id) {
					var findObj = {};

					if ($scope.settings.externalIdProp === '') {
						findObj[$scope.settings.idProp] = id;
					} else {
						findObj[$scope.settings.externalIdProp] = id;
					}

					return findObj;
				}

				function clearObject(object) {
					for ( var prop in object) {
						delete object[prop];
					}
				}

				if ($scope.singleSelection) {
					if (angular.isArray($scope.model)
							&& $scope.model.length === 0) {
						clearObject($scope.model);
					}
				}

				if ($scope.settings.closeOnBlur) {
					$document.on('click', function(e) {
						var target = e.target.parentElement;
						var parentFound = false;

						while (angular.isDefined(target) && target !== null
								&& !parentFound) {
							if (_.contains(target.className.split(' '),
									'multiselect-parent')
									&& !parentFound) {
								if (target === $dropdownTrigger) {
									parentFound = true;
								}
							}
							target = target.parentElement;
						}

						if (!parentFound) {
							$scope.$apply(function() {
								$scope.open = false;
							});
						}
					});
				}

				$scope.getButtonText = function() {

					if ($scope.settings.dynamicTitle
							&& ($scope.model.length > 0 || (angular
									.isObject($scope.model) && _
									.keys($scope.model).length > 0))) {

						var totalSelected = '';

						angular.forEach($scope.model, function(value, key) {
							totalSelected += value[$scope.settings.idProp]
									+ ', ';
						});
						return totalSelected;

					} else {
						return $scope.texts.buttonDefaultText;
					}
				};

				$scope.getPropertyForObject = function(object, property) {
					if (angular.isDefined(object)
							&& object.hasOwnProperty(property)) {
						return object[property];
					}

					return '';
				};

				$scope.setSelectedItem = function(id, dontRemove) {
					var findObj = getFindObj(id);
					var finalObj = null;

					if ($scope.settings.externalIdProp === '') {
						finalObj = _.find($scope.options, findObj);
					} else {
						finalObj = findObj;
					}

					if ($scope.singleSelection) {
						clearObject($scope.model);
						angular.extend($scope.model, finalObj);
						$scope.externalEvents.onItemSelect(finalObj);
						if ($scope.settings.closeOnSelect)
							$scope.open = false;

						return;
					}

					dontRemove = dontRemove || false;

					var exists = _.findIndex($scope.model, findObj) !== -1;

					if (!dontRemove && exists) {
						$scope.model.splice(_.findIndex($scope.model, findObj),
								1);
						$scope.externalEvents.onItemDeselect(findObj);
					} else if (!exists
							&& ($scope.settings.selectionLimit === 0 || $scope.model.length < $scope.settings.selectionLimit)) {
						$scope.model.push(finalObj);
						$scope.externalEvents.onItemSelect(finalObj);
					}
					if ($scope.settings.closeOnSelect)
						$scope.open = false;

					$scope.$root.isfilter = true;
					$route.reload()
					$location.path('/tree');
				};

				$scope.isChecked = function(id) {
					if ($scope.singleSelection) {
						return $scope.model !== null
								&& angular
										.isDefined($scope.model[$scope.settings.idProp])
								&& $scope.model[$scope.settings.idProp] === getFindObj(id)[$scope.settings.idProp];
					}

					return _.findIndex($scope.model, getFindObj(id)) !== -1;
				};

				$scope.externalEvents.onInitDone();
			}
		};
	}

})();
