(function() {
	'use strict';

	angular.module('app').controller('TreeController', TreeController)
			.directive('tree', tree);

	TreeController.$inject = ['$http','$location', '$rootScope', 'RecursionHelper' ];
	function TreeController($http,$location,$rootScope, RecursionHelper) {
		console.log($rootScope.selectedUseCase);
		delete $rootScope.flash;

		var tc = this;
		if ($rootScope.selectedUseCase != undefined){
			$http({
			       method : "GET",
					url : $location.protocol() + '://' + $location.host()  + ':' + $location.port() + '/zephyrtool/rest/getfeatures' + '?usecase=' + $rootScope.selectedUseCase,
			   }).then(function mySucces(response) {
			       console.log(response.data);
			       var treeData = {
			    		   "id": "1",
			    		   "key": "true",
			    		   "summary" : "parent",
			    		   "children" : response.data
			       };
			       console.log(treeData);
			       
			       tc.treeFamily = treeData;
			   }, function myError(response) {
				   $rootScope.flash = {
							message : "Failed to get features with a status code" +  response.statusText,
							type : true
						};
			   });
		}

	};

	tree.$inject = [ 'RecursionHelper' ];
	function tree(RecursionHelper) {
		return {
			restrict : "E",
			scope : {
				family : '='
			},
			template : '<p style="{{ family.summary === \'parent\' ? \'display:none\' : \'display:block\' }}">{{ family.summary }}</p>' + '<ul>'
					+ '<li ng-repeat="child in family.children">'
					+ '<tree family="child"></tree>' + '</li>' + '</ul>',
			compile : function(element) {
				// Use the compile function from the RecursionHelper,
				// And return the linking function(s) which it returns
				return RecursionHelper.compile(element);
			}
		};
	};
	
	function checkSomething(){
		console.log(1);
	}

})();