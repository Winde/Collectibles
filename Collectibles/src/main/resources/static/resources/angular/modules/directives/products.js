(function(){	
	
	var app = angular.module('products-directives',['products-controllers']);

	app.directive('productSearch', function(){
		return {
			restrict: 'E',
			templateUrl: '/app/snipet/product-search.html',
			controllerAs: 'productListCtrl',
			controller: 'ProductListController',			
			link: function(scope, element, attrs, ng){               
			}
		};		
	});
	
	app.directive('productPage', function(){
		return {			
			restrict: 'E',
			templateUrl: '/app/snipet/product-page.html',	
			scope: {
				product: "=",
				editable: "=",
				forbidedit: "="
			},
			controllerAs: 'productCtrl',
			controller: 'ProductDetailsController'
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
	
	app.directive('productChange', function(){
		return {
			restrict: 'E',
			templateUrl: '/app/snipet/product-change.html',
			scope: {
				product: "=",
				createnew: "@"
			},
			controller: 'ProductChangeController',
			controllerAs: 'productChangeCtrl'			
		};		
	});
	
	app.directive('productUpload', function(){
		return {
			restrict: 'E',
			templateUrl: '/app/snipet/product-upload.html',
			controller: 'ProductChangeController',
			controllerAs: 'productChangeCtrl'			
		};		
	});
	
})();