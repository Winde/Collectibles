(function(){	
	
	angular.module('product')
	.directive('productUpload', function(){
		return {
			restrict: 'E',
			templateUrl: '/app/snipet/product-upload.html',
			controller: 'ProductChangeController',
			controllerAs: 'productChangeCtrl'			
		};		
	});
	
})();