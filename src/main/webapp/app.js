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

		var items = getUsecases($http,$location);

		if (items != null && items.data != null) {
			$rootScope.usecases = items.data;
		} else {
			$rootScope.flash = {
				message : "Failed to get usecases",
				type : true
			};
		}

		$rootScope.update = function() {
			console.log($rootScope.selectedUseCase);
			$route.reload()
			$location.path('/tree');
		}
	}

	function getUsecases($http,$location) {
		return $http({
			method : 'GET',
			url : $location.protocol() + '://' + $location.host()  + ':' + $location.port() + '/zephyrtool/rest/getusecases',
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
