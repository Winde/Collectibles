(function(){	
	
	angular.module('hierarchy')
	.directive('hierarchyTree', function(){
		return {
			restrict: 'E',
			scope: {
				hierarchies: "=",
				isAuthenticated: "&",
				isAdmin: "&"
			},
			templateUrl: 'snipet/hierarchy-tree.html',
			controllerAs: 'hierarchyCtrl',
			controller: 'HierarchyCreateController'			
		};		
	});
	
	
})();
