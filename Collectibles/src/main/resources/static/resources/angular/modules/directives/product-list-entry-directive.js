(function(){	
	
	angular.module('product')
	.directive('productListEntry', function(){
		return {
			restrict: 'E',	
			templateUrl: '/app/snipet/product-list-entry.html',
			controller: 'ProductDetailsController',
			controllerAs: 'productManagementCtrl',			
			require: '^productSearch',
			link: function(scope, element, attrs,productSearchCtrl) {
				scope.remove = function(){
					productSearchCtrl.remove(scope.product);
				}
				scope.update = function(){
					productSearchCtrl.update(scope.product);
				}
			}
				
		};		
	});
	
})();