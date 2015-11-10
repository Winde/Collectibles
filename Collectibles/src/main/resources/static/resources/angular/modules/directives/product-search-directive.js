(function(){	
	
	angular.module('product')
	.directive('productSearch', function(){
		return {
			restrict: 'E',
			templateUrl: '/app/snipet/product-search.html',
			controllerAs: 'productListCtrl',
			controller: 'ProductListController',			
			link: function(scope, element, attrs, ng){               
			}
		};		
	});
	
})();