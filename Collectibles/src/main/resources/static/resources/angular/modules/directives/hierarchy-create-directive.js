(function(){	
	
	angular.module('hierarchy')
	.directive('hierarchyCreate', function(){
		return {
			restrict: 'E',
			scope: {
				hierarchies: "=",
				isAuthenticated: "&"
			},
			templateUrl: '/app/snipet/hierarchy-create.html',
			controllerAs: 'hierarchyCtrl',
			controller: 'HierarchyCreateController'			
		};		
	})

})();
