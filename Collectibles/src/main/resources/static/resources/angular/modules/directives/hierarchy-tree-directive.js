(function(){	
	
	angular.module('hierarchy')
	.directive('hierarchyTree', function(){
		return {
			restrict: 'E',
			scope: {
				hierarchies: "=",
				isAuthenticated: "&"
			},
			templateUrl: '/app/snipet/hierarchy-tree.html',
			controllerAs: 'hierarchyCtrl',
			controller: 'HierarchyCreateController'			
		};		
	});
	
	
})();
