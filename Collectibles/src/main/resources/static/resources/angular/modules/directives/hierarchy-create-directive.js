(function(){	
	
	angular.module('hierarchy')
	.directive('hierarchyCreate', function(){
		return {
			restrict: 'E',
			scope: {
				hierarchies: "=",
				isAuthenticated: "&",
				isAdmin: "&"
			},
			templateUrl: '/app/snipet/hierarchy-create.html',
			controllerAs: 'hierarchyCtrl',
			controller: 'HierarchyCreateController'			
		};		
	})

})();
