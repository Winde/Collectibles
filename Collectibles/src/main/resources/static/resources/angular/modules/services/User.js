(function(){
	
	angular.module('user')
	.factory('User', function ProductFactory(Properties,$http){
		var factory = this;
		
				
		return {		
			all: function() {				
				return $http({ 
					method: 'GET', 
					url: Properties.baseDomain+'/users/list/', 
					progressbar: true 
				});
			}		
		}
	});
	
	
})();