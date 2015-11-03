(function(){
	var app = angular.module('collections', 
	['ngRoute',
	 'products-controllers',
	 'products-directives',
	 'generic-directives']);
	
	
	
	app.controller('NavbarController',['$scope','$location', function($scope, $location){
		
		
		$scope.isActive = function (viewLocation) { 
	        return viewLocation === $location.path();
	    };
	}]);
	

})();

