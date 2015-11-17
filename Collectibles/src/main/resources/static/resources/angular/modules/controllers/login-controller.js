(function(){
	angular.module('login')
	.controller('LoginController',
	        function($scope,$uibModalInstance ,Auth,Message,$location){
	        
				$scope.login = function(){
					Auth.login(
					$scope.credentials,
					function(){
						$uibModalInstance.close();
					},
					function(){						
						Auth.logout();
					});										
				}
				
	});
	 
})();