(function(){
	angular.module('collections')
	.controller('NavigationController',['$rootScope','$scope','$location','$uibModal','Auth', 
	                                       function($rootScope,$scope,$location,$uibModal,Auth){

		
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
		
		$scope.isAdmin = function(){
			return Auth.isAdmin();
		}
		
	
		$scope.isActive = function (viewLocation) { 
	        return viewLocation === $location.path();
	    };
	    
	    $scope.openLogin = function(){
	    	var modalInstance = $uibModal.open({
    	      animation: true,
    	      windowClass: "modal fade in",
    	      templateUrl: 'templates/login/login.html',
    	      controller: 'LoginController',
    	      size: 'lg',
    	      resolve: {    	        
    	      }
	    	});
	    }
	}]);

})();