(function(){	
	
	angular.module('product')
	.directive('productUpload', function(){
		return {
			restrict: 'E',
			templateUrl: 'snipet/product-upload.html',
			controller: 'ProductChangeController',
			controllerAs: 'productChangeCtrl'			
		};		
	});
	
})();