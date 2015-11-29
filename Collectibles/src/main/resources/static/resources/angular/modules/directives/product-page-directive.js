(function(){	
	
	angular.module('product')
	.directive('productPage', function(Domain){
		return {			
			restrict: 'E',
			templateUrl: 'snipet/product-page.html',	
			scope: {
				product: "=",
				editable: "=",
				forbidedit: "=",
				isAuthenticated: "&",
				isLoggedIn: "&",
				isAdmin: "&"
			},
			link: function(scope,element,attrs){
				scope.domain = Domain.base();				
			},
			controllerAs: 'productCtrl',
			controller: 'ProductDetailsController'
		};		
	});
	
})();