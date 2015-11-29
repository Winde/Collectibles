(function(){	
	
	angular.module('product')
	.directive('productChange', function(){
		return {
			restrict: 'E',
			templateUrl: 'snipet/product-change.html',
			scope: {
				product: "=",
				createnew: "@",
				isAuthenticated: "&",
				isAdmin: "&"
			},
			link: function(scope,element,attrs){
			},
			controller: 'ProductChangeController',
			controllerAs: 'productChangeCtrl'			
		};		
	});
	
})();