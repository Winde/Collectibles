(function(){

	angular.module('hierarchies-controllers',[])
	.controller('HierarchyCreateController',
			['$scope','Hierarchy','Message',
	        function($scope,Hierarchy,Message){
			
				var controller = this;
				
				$scope.hierarchy = {};
				$scope.parent = {};
				if ($scope.hierarchies== null){
					$scope.hierarchies = [];
				}
				this.deleting = false;
				
				this.updateHierarchies = function(){
					Hierarchy.root()
					.success(function(data){	
						$scope.hierarchies =[];
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
				
				this.remove = function(hierarchy){
					if (!controller.deleting){
						Message.confirm("Are you sure?", function(){
							controller.deleting = true;
							Hierarchy.remove(hierarchy)
							.success(function(){
								controller.updateHierarchies();	
							})
							.catch(function(){
								
							})
							.finally(function(){
								$scope.ajax = false;
								controller.deleting = false;
							});
						});
					} else {
						Message.alert("Operation in progress",true)
					}
										
				};
				
				this.updateHierarchies();
				
			}
				
	]);
	
})();