(function(){
	
	angular.module('user')
	.factory('User', function ProductFactory(Domain,$http){
		var factory = this;
		
				
		return {		
			all: function() {				
				return $http({ 
					method: 'GET', 
					url: Domain.base()+'/users/list/', 
					progressbar: true 
				});
			}		
		}
	});
	
	
})();