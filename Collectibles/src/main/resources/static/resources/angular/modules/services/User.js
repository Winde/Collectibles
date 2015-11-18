(function(){
	
	angular.module('user')
	.factory('User', function ProductFactory($http){
		var factory = this;
		
				
		return {		
			all: function() {				
				return $http({ 
					method: 'GET', 
					url: '/users/list/', 
					progressbar: true 
				});
			}		
		}
	});
	
	
})();