(function(){	
	
	angular.module('hierarchies-directives',[])
	.directive('hierarchyCreate', function(){
		return {
			restrict: 'E',
			scope: {
				hierarchies: "="
			},
			templateUrl: '/app/snipet/hierarchy-create.html',
			controllerAs: 'hierarchyCtrl',
			controller: 'HierarchyCreateController'			
		};		
	})
	.directive('hierarchyTree', function(){
		return {
			restrict: 'E',
			scope: {
				hierarchies: "="
			},
			templateUrl: '/app/snipet/hierarchy-tree.html',
			controllerAs: 'hierarchyCtrl',
			controller: 'HierarchyCreateController'			
		};		
	});
	
})();
