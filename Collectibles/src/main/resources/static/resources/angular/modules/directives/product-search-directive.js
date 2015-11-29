(function(){	
	
	angular.module('product')
	.directive('productSearch', function(){
		return {
			restrict: 'E',
			templateUrl: 'snipet/product-search.html',
			controllerAs: 'productListCtrl',
			controller: 'ProductListController',			
			link: function(scope, element, attrs, ng){
				scope.positionInitialGrid = function(){
					
					
					var grid = $(element).find('.productGrid');
					if (grid.length>0){
						var windowOffset = $(window).scrollTop();
						var gridOffset = grid.offset().top -150;
						if (windowOffset && gridOffset){
							if (windowOffset > gridOffset + 200){
								$('html, body').stop().animate({
							        scrollTop: $('.productGrid').offset().top -150
							    }, 500);
							}
						}
					}
					//setTimeout(function () {
				        //window.scrollTo(0, $('.productGrid') - 100)
				    //}, 20);
				}
			}
		};		
	});
	
})();