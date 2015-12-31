(function(){	
	
	angular.module('product')
	.directive('productPage', function(Properties){
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
				scope.domain = Properties.baseDomain;				
			},
			controllerAs: 'productCtrl',
			controller: 'ProductDetailsController'
		};		
	});
	
})();