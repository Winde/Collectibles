(function(){
	var app = angular.module('collections', 
	['ngRoute',
	 'ngSanitize',
	 'ngAnimate',
	 'login-services',
	 'login-controllers',
	 'products-controllers',
	 'products-directives',
	 'products-services',
	 'images-services',	 
	 'hierarchies-controllers',
	 'hierarchies-directives',
	 'hierarchies-services',
	 'generic-services',
	 'generic-directives']);
	
	
	
	app.controller('NavigationController',['$rootScope','$scope','$location','Auth', function($rootScope,$scope, $location,Auth){
				
		$scope.isAuthenticated = function(){			
			return Auth.isloggedIn();
		} 
		
		$scope.isActive = function (viewLocation) { 
	        return viewLocation === $location.path();
	    };
	}]);
	

})();

