(function(){
	
	var app = angular.module('products-directives',['products-controllers']);

	app.directive('productSearch', function(){
		return {
			restrict: 'E',
			templateUrl: '/app/snipet/product-search.html',
			controllerAs: 'productListCtrl',
			controller: 'ProductListController'
		};		
	});
	
	app.directive('productPage', function(){
		return {
			restrict: 'E',
			templateUrl: '/app/snipet/product-page.html'
		};		
	});
	
	app.directive('productListEntry', function(){
		return {
			restrict: 'E',
			templateUrl: '/app/snipet/product-list-entry.html',
			controller: 'ProductListController',
			controllerAs: 'productManagementCtrl'			
		};		
	});
	
	app.directive('productCreate', function(){
		return {
			restrict: 'E',
			templateUrl: '/app/snipet/product-create.html',
			controller: 'ProductCreationController',
			controllerAs: 'productCtrl'			
		};		
	});
	
})();