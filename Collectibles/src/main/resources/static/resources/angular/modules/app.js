(function(){
	var app = angular.module('collections', 
	['ngRoute',
	 'products-controllers',
	 'products-directives',
	 'products-services',
	 'images-services',	 
	 'hierarchies-controllers',
	 'hierarchies-directives',
	 'hierarchies-services',
	 'generic-services',
	 'generic-directives']);
	
	
	
	app.controller('NavbarController',['$scope','$location', function($scope, $location){
		
		
		$scope.isActive = function (viewLocation) { 
	        return viewLocation === $location.path();
	    };
	}]);
	

})();

