(function() {
	'use strict';
	// App Module: the name AngularStore matches the ng-app attribute in the
	// main <html> tag
	// the route provides parses the URL and injects the appropriate partial
	// page
	angular.module('app', [ 'ngRoute', 'ngCookies' ]).config(config).run(run)
			.factory('RecursionHelper', RecursionHelper);
	config.$inject = [ '$routeProvider', '$locationProvider' ];
	function config($routeProvider, $locationProvider) {

		$routeProvider.when('/tree', {
			controller : 'TreeController',
			templateUrl : 'tree/tree.view.html',
			controllerAs : 'tc'
		}).otherwise({
			redirectTo : '/'
		});
	}

	run.$inject = [ '$rootScope', '$route', '$location', '$cookieStore',
			'$http' ];
	function run($rootScope, $route, $location, $cookieStore, $http) {

		delete $rootScope.flash;
		getUsecases($rootScope,$http,$location);
		$rootScope.functionalities = ['functionality','scalability','performance','debuggability','limits','upgradability','backward compatibility','security','ui','negative case'];
		$rootScope.components = ['AllAgents','apm-core','apm-db','apm-dotnet','apm-java','e2e','eum','platform-services-dashboards','ui-platform'];
		$rootScope.update = function() {
			$rootScope.isfilter = false;
			$route.reload()
			$location.path('/tree');
		}
		
		$rootScope.filer = function() {
			$rootScope.isfilter = true;
			$route.reload()
			$location.path('/tree');
		}
	}

	function getUsecases($rootScope,$http,$location) {
		$http({
		       method : "GET",
		       url : $location.protocol() + '://' + $location.host()  + ':' + $location.port() + '/zephyrtool/rest/getusecases'
		   }).then(function mySucces(response) {
		       $rootScope.usecases = response.data;
		   }, function myError(response) {
			   $rootScope.flash = {
						message : "Failed to get usecases with a status code" +  response.statusText,
						type : true
					};
		   });

	}

	RecursionHelper.$inject = [ '$compile' ];
	function RecursionHelper($compile) {
		return {

			compile : function(element, link) {
				// Normalize the link parameter
				if (angular.isFunction(link)) {
					link = {
						post : link
					};
				}

				// Break the recursion loop by removing the contents
				var contents = element.contents().remove();
				var compiledContents;
				return {
					pre : (link && link.pre) ? link.pre : null,
					/**
					 * Compiles and re-adds the contents
					 */
					post : function(scope, element) {
						// Compile the contents
						if (!compiledContents) {
							compiledContents = $compile(contents);
						}
						// Re-add the compiled contents to the element
						compiledContents(scope, function(clone) {
							element.append(clone);
						});

						// Call the post-linking function, if any
						if (link && link.post) {
							link.post.apply(null, arguments);
						}
					}
				};
			}
		};
	}
})();
