(function(){
	var app = angular.module('collections', 
	['products-controllers',
	 'products-directives',
	 'generic-directives']);

	app.controller('NavbarController', function(){
		this.tab = 1;
		
		this.selectTab = function(setTab){
			this.tab = setTab;			
		}
		
		this.isSelected = function(checkTab){
			return this.tab === checkTab;
		}
	});
	

})();

