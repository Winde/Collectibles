(function(){	
	
	angular.module('product')
	.directive('productConnectorParse', function(){
		return {
			restrict: 'E',
			scope: {},
			templateUrl: '/app/snipet/product-connector-parse.html',
			controller: 'ProductChangeController',
			controllerAs: 'productChangeCtrl'			
		};		
	});
	
})();