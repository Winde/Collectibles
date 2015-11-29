(function(){	
	
	angular.module('product')
	.directive('productConnectorParse', function(){
		return {
			restrict: 'E',
			scope: {},
			templateUrl: 'snipet/product-connector-parse.html',
			controller: 'ProductChangeController',
			controllerAs: 'productChangeCtrl'			
		};		
	});
	
})();