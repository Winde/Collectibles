(function(){

	angular.module('collections').config(function($httpProvider,$routeProvider){
		
		$httpProvider.interceptors.push('httpInjectProgress')
		$httpProvider.interceptors.push('httpInjectAuth');
		$httpProvider.interceptors.push('httpInjectParserMessage');
		
		var routerConfig = this;

		this.secured = function($location,$q,Auth){
			var deferred = $q.defer();			
			if (!Auth.isloggedIn()){							
				deferred.reject();
		        $location.url('/products/');
			} else {
				deferred.resolve();
			}
		}
				
		$routeProvider
		//.when('/login/', {
		//	templateUrl : '/app/templates/login/login.html',
		//	secured: false
		//})
		.when('/products/', {
			templateUrl: '/app/templates/products/search.html',
			secured: false,
			reloadOnSearch: false
		})
		.when('/product/:id', {
			templateUrl: '/app/templates/products/details.html',
			secured: false
		})
		.when('/products/create/', {
			templateUrl: '/app/templates/products/create.html',
			secured: true,
			resolve: { loggedIn: routerConfig.secured }
		})
		.when('/hierarchy/create/', {
			templateUrl: '/app/templates/hierarchies/create.html',
			secured: true,
			resolve: { loggedIn: routerConfig.secured }
		})
		.otherwise({
			redirectTo: '/products/'
		});				
	});
	
})();

