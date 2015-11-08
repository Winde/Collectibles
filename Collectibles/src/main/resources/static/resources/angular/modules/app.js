(function(){
	var app = angular.module('collections', 
	['ngRoute',
	 'ngSanitize',
	 'ngAnimate',
	 'ngCookies',
	 'ngProgress',
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
	
	
	
	app.controller('NavigationController',['$rootScope','$scope','$location','$routeParams','Auth','ngProgressFactory', 
	                                       function($rootScope,$scope,$location,$routeParams,Auth,ngProgressFactory){
		

		Auth.checkSessionIsSet();
		
		$rootScope.$on('$routeChangeStart', function (event, next) {            
            if (next.secured !== undefined) {            	
            	$rootScope.secured = next.secured;
            }
        });
		
		$scope.logout = function(){
			Auth.logout();
			console.log("Scope is Secured? " + $scope.secured);
			if ($scope.secured == true){
				$location.path('/products').replace();
			}
		}
		
		$scope.isAuthenticated = function(){			
			return Auth.isloggedIn();
		} 
		
		$scope.isActive = function (viewLocation) { 
	        return viewLocation === $location.path();
	    };
	}]);
	

})();

