(function() {
	'use strict';
	// App Module: the name AngularStore matches the ng-app attribute in the main <html> tag
	// the route provides parses the URL and injects the appropriate partial page
	angular.module('app', [ 'ngRoute', 'ngCookies' ]).config(config).run(run).factory('RecursionHelper',RecursionHelper);
	config.$inject = [ '$routeProvider', '$locationProvider' ];
	function config($routeProvider, $locationProvider) {
		
		$routeProvider.when('/tree', {
			controller : 'TreeController',
			templateUrl : 'tree/tree.view.html',
			controllerAs : 'tc'
		})
		.otherwise({
			redirectTo : '/'
		});
	}

	run.$inject = [ '$rootScope', '$location', '$cookieStore', '$http'];
	function run($rootScope, $location, $cookieStore, $http) {

		$rootScope.usecases = [".NET", "Java", "Python"];
		$rootScope.navigatetotreeview = function(path) {
			$location.path(path);
		}
	}
	
	RecursionHelper.$inject = [ '$compile' ];
	function RecursionHelper($compile){
		return {
			
			compile: function(element, link){
				// Normalize the link parameter
				if(angular.isFunction(link)){
					link = { post: link };
				}

				// Break the recursion loop by removing the contents
				var contents = element.contents().remove();
				var compiledContents;
				return {
					pre: (link && link.pre) ? link.pre : null,
					/**
					 * Compiles and re-adds the contents
					 */
					post: function(scope, element){
						// Compile the contents
						if(!compiledContents){
							compiledContents = $compile(contents);
						}
						// Re-add the compiled contents to the element
						compiledContents(scope, function(clone){
							element.append(clone);
						});

						// Call the post-linking function, if any
						if(link && link.post){
							link.post.apply(null, arguments);
						}
					}
				};
			}
		};
	}
})();
