(function() {
	'use strict';

	angular.module('app').controller('TreeController', TreeController)
			.directive('tree', tree);

	TreeController.$inject = ['$http','$location', '$rootScope', 'RecursionHelper' ];
	function TreeController($http,$location,$rootScope, RecursionHelper) {
		console.log($rootScope.selectedUseCase);
		var tc = this;
		if ($rootScope.selectedUseCase != undefined){
			var items = getUseCaseByFeature($http,$location,$rootScope.selectedUseCase);
		}
		if ($rootScope.selectedUseCase != undefined
				&& $rootScope.selectedUseCase == "Technical Debt") {
			tc.treeFamily = {
				"id" : "496544",
				"key" : "ZEP-55",
				"summary" : "Test Feature",
				"children" : []
			};
		}

		if ($rootScope.selectedUseCase != undefined
				&& $rootScope.selectedUseCase == "APM Functional") {

			tc.treeFamily = {
				"id" : "494858",
				"key" : "ZEP-43",
				"summary" : "Business Transaction OOTB Discovery",
				"children" : [ {
					"id" : "494859",
					"key" : "ZEP-44",
					"summary" : "Java BT OOTB ",
					"children" : [ {
						"id" : "494861",
						"key" : "ZEP-45",
						"summary" : "Servlet OOTB",
						"children" : [{
			                  "id": "494903",
			                  "key": "ZEP-52",
			                  "summary": "new test",
			                  "children": []
			                }]
					}, {
						"id" : "494863",
						"key" : "ZEP-46",
						"summary" : "Struts OOTB",
						"children" : []
					}, {
						"id" : "494864",
						"key" : "ZEP-47",
						"summary" : "Web Service OOTB",
						"children" : []
					}, {
						"id" : "494865",
						"key" : "ZEP-48",
						"summary" : "Spring OOTB",
						"children" : []
					}, {
						"id" : "494866",
						"key" : "ZEP-49",
						"summary" : "EJB OOTB",
						"children" : []
					}, {
						"id" : "494867",
						"key" : "ZEP-50",
						"summary" : "JMS OOTB",
						"children" : []
					}, {
						"id" : "494868",
						"key" : "ZEP-51",
						"summary" : "Binary Remoting OOTB",
						"children" : []
					} ]
				} ]
			};
		}

	};

	tree.$inject = [ 'RecursionHelper' ];
	function tree(RecursionHelper) {
		return {
			restrict : "E",
			scope : {
				family : '='
			},
			template : '<p>{{ family.summary }}</p>' + '<ul>'
					+ '<li ng-repeat="child in family.children">'
					+ '<tree family="child"></tree>' + '</li>' + '</ul>',
			compile : function(element) {
				// Use the compile function from the RecursionHelper,
				// And return the linking function(s) which it returns
				return RecursionHelper.compile(element);
			}
		};
	};
	
	function getUseCaseByFeature($http,$location,usecasename){
		return $http({
			method : 'GET',
			url : $location.protocol() + '://' + $location.host()  + ':' + $location.port() + '/zephyrtool/rest/getfeatures' + '?usecase=' + usecasename,
		})
				.then(
						handleSuccess,
						handleError('Error while getting usecases. Please try again later.'));

		// private functions
		function handleSuccess(data) {
			return data;
		}

		function handleError(error) {
			return function() {
				return {
					success : false,
					message : error
				};
			};
		}
	}

})();