(function() {
	'use strict';
	// App Module: the name AngularStore matches the ng-app attribute in the main <html> tag
	// the route provides parses the URL and injects the appropriate partial page
	angular.module('app', [ 'ngRoute', 'ngCookies' ]).config(config).run(run).service('sharedProperties',sharedProperties).factory('superCache',superCache);
	config.$inject = [ '$routeProvider', '$locationProvider' ];
	function config($routeProvider, $locationProvider) {
		
		$routeProvider.when('/', {
			controller : 'ProductsController',
			templateUrl : 'products/products.view.html',
			controllerAs : 'pc'
		})
		.otherwise({
			redirectTo : '/'
		});
	}

	run.$inject = [ '$rootScope', '$location', '$cookieStore', '$http','superCache' ];
	function run($rootScope, $location, $cookieStore, $http,superCache) {

		$rootScope.usecases = [".NET", "Java", "Python"];
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
	
	superCache.$inject = [ '$cacheFactory' ];
	function superCache($cacheFactory){
		 return $cacheFactory('super-cache');
	}
})();
