(function(){	
	
	angular.module('hierarchies-directives',[])
	.directive('hierarchyCreate', function(){
		return {
			restrict: 'E',
			templateUrl: '/app/snipet/hierarchy-create.html',
			controllerAs: 'hierarchyCtrl',
			controller: 'HierarchyCreateController'			
		};		
	});
	
})();
