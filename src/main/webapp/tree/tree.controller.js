(function() {
	'use strict';

	angular.module('app').controller('TreeController', TreeController).directive('tree',tree);

	TreeController.$inject = [ 'RecursionHelper' ];
	function TreeController( RecursionHelper) {

		var tc = this;
		 tc.treeFamily = {
			        name : "Feature",
			        children: [{
			            name : "TestSet1",
			            children: [{
			                name : "Test1",
			                children: []
			            },{
			                name : "Test2",
			                children: []
			            },{
			                name : "Test3",
			                children: []
			            }]
			        }, {
			            name: "TestSet2",
			            children: []
			        }]
			    };
	};
	
	tree.$inject = [ 'RecursionHelper' ];
	function tree( RecursionHelper) {
		 return {
		        restrict: "E",
		        scope: {family: '='},
		        template: 
		            '<p>{{ family.name }}</p>'+
		            '<ul>' + 
		                '<li ng-repeat="child in family.children">' + 
		                    '<tree family="child"></tree>' +
		                '</li>' +
		            '</ul>',
		        compile: function(element) {
		            // Use the compile function from the RecursionHelper,
		            // And return the linking function(s) which it returns
		            return RecursionHelper.compile(element);
		        }
		    };
	};

})();