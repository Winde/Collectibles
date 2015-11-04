(function(){
	
	angular.module('hierarchies-services',[])
	.factory('Hierarchy', function HierarchyFactory($http){
		return {
			root: function(){				
				return $http.get('/hierarchy/root/');				
			}			
		}
		
	});
	
})();