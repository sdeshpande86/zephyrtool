(function() {
	'use strict';

	angular.module('app').controller('TreeController', TreeController)
			.directive('tree', tree);

	TreeController.$inject = [ '$http', '$location', '$rootScope',
			'RecursionHelper' ];
	function TreeController($http, $location, $rootScope, RecursionHelper) {
		delete $rootScope.flash;
		var tc = this;

		if ($rootScope.selectedUseCase != undefined) {
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
								console.log(response.data);
								console.log($rootScope.isfilter);

								var treeData = {
									"id" : "1",
									"key" : "true",
									"summary" : "parent",
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
											var componentsData = false;
											var componentsUndefined = false;
											if ($rootScope.selectedFunctionality) {
												if (node.testType
														&& node.testType == $rootScope.selectedFunctionality) {
													testTypeData = true;
												}
												if (!node.testType) {
													testTypeUndefined = true;
												}
											}
											if ($rootScope.selectedComponent) {
												if (!node.components) {
													componentsUndefined = true;
												}
												if (node.components) {
													for ( var i in node.components) {
														if (node.components[i] == $rootScope.selectedComponent) {
															componentsData = true;
														}
													}
												}
											}

											if ($rootScope.selectedFunctionality
													&& $rootScope.selectedComponent) {
												keep = testTypeData
														&& componentsData;
												keep = testTypeData
														&& componentsUndefined;
												keep = testTypeUndefined
														&& componentsData;
											}
											
											if ($rootScope.selectedFunctionality){
												keep = testTypeData || testTypeUndefined;
											}
											
											if ($rootScope.selectedComponent){
												keep = componentsData || componentsUndefined;
											}

											console.log(node.summary, keep);

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

	tree.$inject = [ '$rootScope', 'RecursionHelper' ];
	function tree($rootScope, RecursionHelper) {
		return {
			restrict : "E",
			scope : {
				family : '='
			},
			template : '<p style="{{ family.summary == \'parent\' ? \'display:none\' : \'display:block\' }}">{{ family.summary }}</p>'
					+ '<ul>'
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