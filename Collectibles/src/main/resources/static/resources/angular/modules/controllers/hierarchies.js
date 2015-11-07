(function(){

	angular.module('hierarchies-controllers',[])
	.controller('HierarchyCreateController',
			['$scope','Hierarchy','Message',
	        function($scope,Hierarchy,Message){
			
				var controller = this;
				
				$scope.hierarchy = {};
				$scope.parent = {};
				$scope.hierarchies = [];
				
				
				this.updateHierarchies = function(){
					Hierarchy.root()
					.success(function(data){											
						Hierarchy.calculateTree(data,$scope.hierarchies);			
					})
					.catch(function(){	
						Message.alert("There was an error");
					})
					.finally(function(){			
					});					
				};
								
				this.createHierarchy = function(){
					
					Hierarchy.create($scope.hierarchy,$scope.parent)
					.success(function(data){		
						Message.success("Hierarchy was created");
												
						$scope.hierarchy = {};					
						controller.updateHierarchies();	
					})
					.catch(function(){	
						Message.alert("There was an error");
					})
					.finally(function(){			
					});		
				};
				
				this.updateHierarchies();
				
			}
				
	]);
	
})();