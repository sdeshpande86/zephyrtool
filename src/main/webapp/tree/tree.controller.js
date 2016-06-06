(function() {
	'use strict';

	angular.module('app').controller('TreeController', TreeController)
			.directive('tree', tree);

	TreeController.$inject = [ '$http', '$location', '$rootScope',
			'RecursionHelper' ];
	function TreeController($http, $location, $rootScope, RecursionHelper) {
		delete $rootScope.flash;
		var tc = this;
		if ($rootScope.selectedUseCase) {
			$http(
					{
						method : "GET",
						url : $location.protocol() + '://' + $location.host() + ':' + $location.port()
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

								tc.treeFamily = treeData;
							},
							function myError(response) {
								$rootScope.flash = {
									message : "Failed to get features with a status code"
											+ response.statusText,
									type : true
								};
							});
		}

	}
	;

	tree.$inject = [ '$rootScope', 'RecursionHelper','$location' ];
	function tree($rootScope, RecursionHelper,$location) {
		return {
			restrict : "AE",
			scope : {
				family : '='
			},
			template :'<div style="{{ family.summary == \'parent\' ? \'display:none\' : \'display:block\' }}">'
					+ '<div style="display:inline-block" ng-include="\'images/feature.svg\'" ng-show="{{family.issueType == \'Feature\'}}"></div>'
					+ '<div style="display:inline-block" ng-include="\'images/testset.svg\'" ng-show="{{family.issueType == \'Test Set\'}}"></div>'
					+ '<div style="display:inline-block" ng-include="\'images/test.svg\'" ng-show="{{family.issueType == \'Test\'}}"></div>'
					+ '<a target="_blank" href="https://singularity.jira.com/issues/?jql=project=\'Zephyr POC\' and Hierarchy ~ \''
					+ '{{family.summary}}'
					+ '\'">'
					+ ' {{ family.summary }}'
					+ '</a>'
					+ '<div style="display:inline-block;padding-left:10px" ng-hide="{{family.summary == undefined || family.summary == null}}">'
					+ '<a target="_blank" href="'+$location.protocol() + '://' + $location.host() + ':' + $location.port() + '/zephyrtool/rest/createissue' + '?usecase=\'{{$root.selectedUseCase}}\'&parentissue=\'{{family.key}}\'">'

					+ '<span class="glyphicon glyphicon-plus" aria-hidden="true">'
					+ '</span></a></div>'
					+ '</div>'
					+ '<ul style="list-style:none">'
					+ '<li ng-repeat="child in family.children">'
					+ '<tree family="child"></tree>' + '</li>' + '</ul>',
			compile : function(element) {
				// Use the compile function from the RecursionHelper,
				// And return the linking function(s) which it returns
				return RecursionHelper.compile(element);
			}

		};
	}
	;

})();