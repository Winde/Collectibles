(function(){

	angular.module('collections').config(function($routeProvider){
		
		$routeProvider.when('/products/', {
			templateUrl: '/app/templates/products/search.html'
		})		
		.when('/products/create/', {
			templateUrl: '/app/templates/products/create.html',									
		})
		.when('/product/:id', {
			templateUrl: '/app/templates/products/details.html',
		});
		
	});
	
})();

