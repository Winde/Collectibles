(function(){
	angular.module('login')
	.controller('LoginController',
			['$scope','Auth','Message','$location',
	        function($scope,Auth,Message,$location){
	        
				$scope.login = function(){
					Auth.login($scope.credentials, function(){						
						Auth.logout();
					});										
				}
				
	}]);
	 
})();