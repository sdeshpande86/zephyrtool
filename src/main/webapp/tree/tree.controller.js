(function() {
	'use strict';

	var app = angular.module('app')
			
	app.controller('TreeController', function($http, $location, $rootScope,$scope,sharedProperties){
		console.log(sharedProperties.getValue("sharedUseCase"));
		$rootScope.sidebarHide = true;

		if(!sharedProperties.getValue("sharedUseCase")){
			$rootScope.selectedUseCase = "Application Monitoring";
		} else{
			$rootScope.selectedUseCase = sharedProperties.getValue("sharedUseCase");
		}
		console.log($rootScope.selectedUseCase);
		
		delete $rootScope.flash;
		if ($rootScope.selectedUseCase) {
			$http(
					{
						method : "GET",
						url : $location.protocol() + '://' + $location.host()
								+ ':' + $location.port()
								+ '/zephyrtool/rest/getfeatures' + '?usecase='
								+ $rootScope.selectedUseCase,
					})
					.then(
							function mySucces(response) {
								var treeData = {
									"id" : "1",
									"key" : "true",
									"summary" : "parent",
									"issueType" : "parent",
									"components" : [],
									"children" : response.data
								};
								if ($rootScope.isfilter) {
									var stack = [];
									var testType = "";
									var comp = "";

									var navigateTree = function(node) {
										if (!node)
											return false;
										else {
											var keep = false;
											var testTypeData = false;
											var testTypeUndefined = false;
											var testTypechecked = false;
											var componentsData = false;
											var componentsUndefined = false;
											var componentChecked = false;
											var testTypeVal = {};
											var componentVal = {};

											angular
													.forEach(
															$rootScope.testType.ids,
															function(value, key) {
																console
																		.log(value);
																if (value) {
																	testTypechecked = true;
																	if (node.testType
																			&& key == node.testType) {
																		testTypeData = true;
																	} else if (!node.testType) {
																		testTypeUndefined = true;
																	}
																}

															});

											angular
													.forEach(
															$rootScope.component.ids,
															function(value, key) {
																if (value) {
																	componentChecked = true;
																	for ( var i in node.components) {
																		if (key == node.components[i])
																			componentsData = true;
																	}
																}
															});

											console.log(node.summary,
													node.testType,
													node.components,
													testTypechecked,
													componentChecked,
													testTypeData,
													componentsData,
													testTypeUndefined);

											if (testTypechecked
													&& componentChecked) {
												keep = testTypeData
														&& componentsData;
											} else if (testTypechecked) {
												keep = testTypeData
														|| testTypeUndefined;
											} else if (componentChecked) {
												keep = componentsData
														|| testTypeUndefined;
											} else if (!testTypechecked
													&& !componentChecked) {
												keep = true;
											}
											if (node.children) {
												var newChildren = [];
												for ( var i in node.children) {
													if (navigateTree(node.children[i]))
														newChildren
																.push(node.children[i]);
												}
												node.children = newChildren;
											}

											if (keep
													|| (node.children && node.children.length > 0))
												return true;
										}
									}

									navigateTree(treeData);
								}

								$scope.treeFamily = treeData;
							},
							function myError(response) {
								$rootScope.flash = {
									message : "Failed to get features with a status code"
											+ response.statusText,
									type : true
								};
							});
		}

	});

	app.directive(
					'tree',
					function() {
						return {
							restrict : 'E', 
							replace : true, 
							scope : {
								t : '=src' 
							},
							template : '<ul><branch ng-repeat="c in t.children" src="c"></branch></ul>'
						};
					})
					
	app.directive('branch', function($http,$compile,$rootScope,$location) {
		return {
			restrict : 'E', 
			replace : true, 
			scope : {
				b : '=src' 
			},
			template : '<li><div style="display:inline-block" ng-include="\'images/subcategory.svg\'" ng-show="{{b.issueType == \'Subcategory\'}}"></div>'
				+ '<div style="display:inline-block" ng-include="\'images/feature.svg\'" ng-show="{{b.issueType == \'Feature\'}}"></div>'
				+ '<div style="display:inline-block" ng-include="\'images/testset.svg\'" ng-show="{{b.issueType == \'Test Set\'}}"></div>'
				+ '<div style="display:inline-block" ng-include="\'images/test.svg\'" ng-show="{{b.issueType == \'Test\'}}"></div>'		
				+ '<a target="_blank" href="https://singularity.jira.com/browse/'
				+ '{{b.key}}'
				+ '">'
				+'{{ b.summary }}</a>'
				+ '<div style="display:inline-block" ng-show="{{b.testCount > 0}}">&nbsp;({{b.testCount}} Tests) </div>'
				+ '<div style="display:inline-block;padding-left:10px" ng-show="{{b.issueType == \'Feature\' || b.issueType == \'Test Set\' || b.issueType == \'Subcategory\'}}">'
				+ '<a target="_blank" href="'
				+ $location.protocol()
				+ '://'
				+ $location.host()
				+ ':'
				+ $location.port()
				+ '/zephyrtool/rest/createissue'
				+ '?usecase={{$root.selectedUseCase}}&parentissue={{b.key}}">'
				+ '<span style="padding-left:10px" class="glyphicon glyphicon-plus" aria-hidden="true">'
				+ '</span></a></div>'
				+ '<a target="_blank" href="https://singularity.jira.com/issues/?jql=project=\'Zephyr POC\' and issuetype = test and Hierarchy ~ \''
				+ '{{b.hierarchy}}'
				+ '\' ORDER BY \'Test Order\'">'
				+ '<span style="padding-left:10px" class="glyphicon glyphicon-search" aria-hidden="true">'
				+ '</span></a>'
				+ '<span style="padding-left:10px" class="glyphicon glyphicon-eye-open" aria-hidden="true">'
				+ '</span>'
				+ '</div>'
				+'</li>',
			link : function(scope, element, attrs) {
				
				var has_children = angular.isArray(scope.b.children);

				if (has_children) {
					element.append('<tree class="treeclass" src="b"></tree>');

					$compile(element.contents())(scope);
				}
				element.on('click', function(event) {
					event.stopPropagation();
					if(event.target.nodeName == 'SPAN' && event.target.className.indexOf('glyphicon-eye-open') !== -1) {	
						$http(
								{
									method : "GET",
									url : $location.protocol() + '://' + $location.host()
											+ ':' + $location.port()
											+ '/zephyrtool/rest/finddependencies' + '?issueKey='
											+ scope.b.key,
								})
								.then(
										function mySucces(response) {
											console.log(response.data);
											if(response.data && response.data != ''){
												window.open('https://singularity.jira.com/issues/?jql=project=\'Zephyr POC\' and issuetype = test and (Hierarchy ~ \'' + scope.b.hierarchy + '\' or id in (' + response.data+ ')) ORDER BY \'Test Order\'', '_blank');
											} else{
												window.open('https://singularity.jira.com/issues/?jql=project=\'Zephyr POC\' and issuetype = test and Hierarchy ~ \'' + scope.b.hierarchy + '\' ORDER BY \'Test Order\'', '_blank');
											}
										},
										function myError(response) {
											$rootScope.flash = {
												message : "Failed to get dependencies for a test"
														+ response.statusText,
												type : true
											};
										});
					}
					if (has_children && event.target.nodeName == 'LI') {
						element.toggleClass('collapsed');
					}
				});
			}
		};
	})


})();