(function(){	
	
	angular.module('product')
	.directive('productListEntry', function(Properties){
		return {
			restrict: 'E',	
			templateUrl: 'snipet/product-list-entry.html',
			controller: 'ProductDetailsController',
			controllerAs: 'productManagementCtrl',			
			require: '^productSearch',
			link: function(scope, element, attrs,productSearchCtrl) {
				scope.remove = function(){
					productSearchCtrl.remove(scope.product);
				}
				scope.domain = Properties.baseDomain;
			}
				
		};		
	});
	
})();