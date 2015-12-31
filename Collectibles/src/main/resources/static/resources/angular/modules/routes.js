(function(){

	angular.module('collections').config(function(Properties,$httpProvider,$routeProvider,$locationProvider){

		//$locationProvider.html5Mode(true);
		
		console.log(Properties.useHtml5Mode);
		
		if (Properties.useHtml5Mode){
			$locationProvider.html5Mode({
				enabled: true,			
			});
			console.log('HTML5 enabled');
		} else  {
			$locationProvider.html5Mode({
				enabled: false,			
			});
			console.log('HTML5 disabled');
		}
		
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
		//	templateUrl : 'templates/login/login.html',
		//	secured: false
		//})
		.when('/products/', {
			templateUrl: 'templates/products/search.html',
			secured: false,
			reloadOnSearch: false
		})
		.when('/product/:id', {
			templateUrl: 'templates/products/details.html',
			secured: false
		})
		.when('/products/create/', {
			templateUrl: 'templates/products/create.html',
			secured: true,
			resolve: { loggedIn: routerConfig.secured }
		})
		.when('/hierarchy/create/', {
			templateUrl: 'templates/hierarchies/create.html',
			secured: true,
			resolve: { loggedIn: routerConfig.secured }
		})
		.otherwise({
			redirectTo: '/products/'
		});				
	});

})();

