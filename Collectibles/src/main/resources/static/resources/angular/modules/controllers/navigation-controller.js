(function(){
	angular.module('collections')
	.controller('NavigationController',['$rootScope','$scope','$location','Auth', 
	                                       function($rootScope,$scope,$location,Auth){

		
		$scope.isCollapsed = true;
		
		Auth.checkSessionIsSet();
		
	
		$rootScope.$on('$routeChangeStart', function (event, next) {            
            if (next.secured !== undefined) {            	
            	$rootScope.secured = next.secured;
            }
        });
		
		
		$scope.logout = function(){
			Auth.logout();					
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