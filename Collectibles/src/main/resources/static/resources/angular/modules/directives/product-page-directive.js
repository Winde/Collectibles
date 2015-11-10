(function(){	
	
	angular.module('product')
	.directive('productPage', function(){
		return {			
			restrict: 'E',
			templateUrl: '/app/snipet/product-page.html',	
			scope: {
				product: "=",
				editable: "=",
				forbidedit: "=",
				isAuthenticated: "&"				
			},
			link: function(scope,element,attrs){
			},
			controllerAs: 'productCtrl',
			controller: 'ProductDetailsController'
		};		
	});
	
})();