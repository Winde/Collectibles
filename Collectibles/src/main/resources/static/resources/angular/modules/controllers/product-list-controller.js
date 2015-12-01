(function(){
	
	angular.module('product')
	.controller('ProductListController',['$scope','$filter','$location','Image','Product','Hierarchy','User','Message',
	                                        function($scope,$filter,$location,Image,Product,Hierarchy,User,Message){
		var controller = this;
				
		$scope.editMode = false;
		$scope.store = {}; 
			
		this.defaultPaginationSize = 50;
		
		if ($scope.$parent && $scope.$parent.root) {
			$scope.root = $scope.$parent.root;
		}
		
		$scope.products = [];
		if ($scope.hierarchies = null || $scope.hierarchies == undefined) {
			$scope.hierarchies = [];
		}
		
		if ($scope.$parent && $scope.$parent.hierarchies) {
			$scope.hierarchies = $scope.$parent.hierarchies;
		}
		
		
		
		this.updateSearch = function(newSearch){
			var searchObject = {};


			if ($scope.hierarchy && $scope.hierarchy.id){ 
				searchObject.hierarchy = $scope.hierarchy.id;
				$scope.connectors = Hierarchy.calculateConnectors(Hierarchy.find($scope.hierarchy.id, $scope.root));
			}
						
			if ($scope.store && $scope.store.identifier && $scope.connectors && $scope.connectors.length>0) {
				$scope.connectors.forEach(function(element){
					if (element && element.identifier && (element.identifier == $scope.store.identifier)){
						searchObject.store = $scope.store.identifier;		
					}
				});
			}
			
			
			if ($scope.seller){ searchObject.seller = $scope.seller; }			
			
			if ($scope.searchTerm){ searchObject.search = $scope.searchTerm; }			
			
			if ($scope.withImages){ searchObject.withImages = $scope.withImages; }
			
			if ($scope.withPrice){ searchObject.withPrice = $scope.withPrice; }
			
			if ($scope.withDriveThruLink){ searchObject.withDriveThruLink = $scope.withDriveThruLink; }
			
			if ($scope.owned){ searchObject.owned = $scope.owned}
			if ($scope.ownedBy){ searchObject.ownedBy = $scope.ownedBy}
			if ($scope.wishedBy){ searchObject.wishedBy = $scope.wishedBy}
			
			if ($scope.page){ searchObject.page = $scope.page } else { $scope.page = 0; searchObject.page = 0; }
			
			if ($scope.maxResults){ searchObject.maxResults = $scope.maxResults } else { $scope.maxResults = 0; searchObject.maxResults = 0; }
			
			if ($scope.sortBy){ searchObject.sortBy = $scope.sortBy; }
			if ($scope.sortOrder){ searchObject.sortOrder = $scope.sortOrder; }
			
			if (newSearch){
				searchObject.page = 0;
				$scope.page = 0;
			}			
						
			$location.search(searchObject);					
		}
		
		this.obtainSearchParameters = function(){
			return $location.search()
		};
		
		this.setSearchParameters = function(){
			var searchObject = controller.obtainSearchParameters();			
			
			if (searchObject.ownedBy){
				searchObject.ownedBy = parseInt(searchObject.ownedBy);
			}
			if (searchObject.wishedBy){
				searchObject.wishedBy = parseInt(searchObject.wishedBy);
			}
						
			if (searchObject.hierarchy){			
				var hierarchy = parseInt(searchObject.hierarchy)
				if (!isNaN(hierarchy)){
					$scope.hierarchy = {id: hierarchy};
					$scope.connectors = Hierarchy.calculateConnectors(Hierarchy.find(hierarchy, $scope.root));
				}				
				
			} else if ($scope.hierarchy == null || $scope.hierarchy == undefined){				
				$scope.hierarchy = {};
			}			
			
			if (searchObject.search){		$scope.searchTerm = searchObject.search;
			} else {	$scope.searchTerm = "";}
			
			if (searchObject.withImages){	$scope.withImages = searchObject.withImages;
			} else {	$scope.withImages = "";}
			
			if (searchObject.withPrice){	$scope.withPrice = searchObject.withPrice;
			} else {	$scope.withPrice = "";}
						
			if (searchObject.withDriveThruLink){	$scope.withDriveThruLink = searchObject.withDriveThruLink;
			} else {	$scope.withDriveThruLink = "";}
			
			if (searchObject.owned){	$scope.owned = searchObject.owned;
			} else {	$scope.owned = "";}
			
			if (searchObject.ownedBy && !isNaN(searchObject.ownedBy)){					
				$scope.ownedBy = searchObject.ownedBy;
			} else {	$scope.ownedBy = "";}
			
			if (searchObject.wishedBy && !isNaN(searchObject.wishedBy)){					
				$scope.wishedBy = searchObject.wishedBy;
			} else {	$scope.wishedBy = "";}
			
			if (searchObject.page){	$scope.page = searchObject.page;
			} else {	searchObject.page = 0; $scope.page= 0; }
			
			if (searchObject.maxResults){	$scope.maxResults = searchObject.maxResults;
			} else {	searchObject.maxResults = controller.defaultPaginationSize; $scope.maxResults= controller.defaultPaginationSize; }
			
			if (searchObject.sortBy){	$scope.sortBy = searchObject.sortBy;
			} else {	searchObject.sortBy = ""; }
			
			if (searchObject.sortOrder){	$scope.sortOrder = searchObject.sortOrder;
			} else {	searchObject.sortOrder = ""; }
			
			if (searchObject.store){	$scope.store.identifier = searchObject.store; } else { $scope.store.identifier = null } 
			if (searchObject.seller){	$scope.seller = searchObject.seller; } else { $scope.seller = null; }	
			
		};
		
		this.updatePriceSearch = function(){
			Message.confirm("Are you sure?", function(){
				var searchObject = controller.obtainSearchParameters();
				
				Product.updatePricesForSearch(searchObject)
				.success(function(data){ 				
				})
				.catch(function(data){
					Message.alert("There was an error");							
				}).finally(function(data){
					
				});
			});
		}
		
		this.updateSeller = function(seller){						
			if (seller==undefined){
				$scope.seller = null;	
				return true;
			} else{
				if ($scope.seller == seller) {
					return false;
				} else{
					$scope.seller = seller
					return true;
				}
			}			
		}
		
		
		
		this.search = function() {
			var searchObject = controller.obtainSearchParameters();
		
			if (searchObject.hierarchy 
					|| (searchObject.searchTerm && searchObject.searchTerm!="" && searchObject.searchTerm.length>2)
					|| (searchObject.seller && searchObject.seller!="")
					
			){
				$scope.processingSearch = true;
				if (searchObject.hierarchy!=null){
					var hierarchyId = parseInt(searchObject.hierarchy);
					if (hierarchyId!=null && !isNaN(hierarchyId)){
						$scope.hierarchies.length = 0;						
						$scope.hierarchies =[];
						var all = { id: $scope.root.id, name: "All", isRoot: true};
						$scope.hierarchies.push(all);
						Hierarchy.calculateTreeFromSelection($scope.root,$scope.hierarchies,hierarchyId,3);
					}						
				}
				Product.search(searchObject)
				.success(function(data){ 
					if (data && data.objects){
						$scope.products = data.objects;	
					}
					if (data && data.maxResults){
						$scope.maxResults = data.maxResults;
					}
					if (data && data.hasNext!=null){
						$scope.hasNext = data.hasNext;
					}	
					if ($scope.positionInitialGrid){
						$scope.positionInitialGrid();
					}
				})
				.catch(function(data){
					Message.alert("There was an error");
					$scope.products = [];				
				}).finally(function(data){
					$scope.processingSearch = false;
				});
			} else {
				$scope.products = [];
			}
		}
		/*
		if (!$scope.users && $scope.isAuthenticated()) {
			User.all()
			.success(function(data){
				$scope.users = data;
			})
			.catch(function(data){
				Message.alert("There was an error");
			})
			.finally(function(data){
				
			});		
		}*/
		//code to obtain users
		User.all()
		.success(function(data){
			$scope.users = [];
			$scope.users.push({id: "", contactName: "anyone"});	        					
			$scope.users = $scope.users.concat(data);
		})
		.catch(function(data){
			Message.alert("There was an error");
		})
		.finally(function(data){
			
		});	
		
		/*
		 $scope.$watch(
             function( $scope ) {                 
                 return( $scope.isAuthenticated() );
             },
             function( newValue ) {
            	 if (newValue === true){
	        		 if (!$scope.users && $scope.isAuthenticated()) {
	        				//Insert here code to obtain users if we decide to only provide to authenticated
	        		  }
            	 }
             }
         );
		*/
		$scope.$on('$locationChangeSuccess', function() {
			controller.setSearchParameters();
			controller.search();
		}); 
		
		if ($scope.root == undefined || $scope.root == null){
			Hierarchy.root()
			.success(function(data){				
				$scope.root = data;				
				if ($scope.hierarchy == null || $scope.hierarchy == undefined){
					$scope.hierarchy = { id: data.id };
				}				
				var all = { id: data.id, name: "All", isRoot: true};
				$scope.hierarchies.push(all);			
				$scope.allHierarchies = [];
				
				var searchParameters = controller.obtainSearchParameters();
				var hierarchyId = null;				
				if (searchParameters && searchParameters.hierarchy){
					hierarchyId = parseInt(searchParameters.hierarchy);					
				}
				/*
				if (hierarchyId && !isNaN(hierarchyId)){
					Hierarchy.calculateTreeFromSelection(data,$scope.hierarchies,hierarchyId,3);	
				} else {
					Hierarchy.calculateTree(data,$scope.hierarchies,3);
				}
				*/
				Hierarchy.calculateTree(data,$scope.hierarchies,3);
				
				Hierarchy.calculateTree(data,$scope.allHierarchies);
				
				$scope.hierarchy = all;				
				controller.setSearchParameters();	
				
				/*
				$scope.hierarchyChildren = []
				
				if ($scope.hierarchy && $scope.hierarchy.id && $scope.allHierarchies && $scope.allHierarchies.length){
					for (var i=0;i<$scope.allHierarchies.length;i++){
						if ($scope.hierarchy.id  && $scope.allHierarchies[i].id && $scope.hierarchy.id == $scope.allHierarchies[i].id){
							if ($scope.allHierarchies[i].children){
								$scope.hierarchyChildren = $scope.allHierarchies[i].children;
							}
						}
					}		
				}
				*/								
				
				if (!angular.equals({}, controller.obtainSearchParameters())){
					controller.search();
				}
				/**/
			})
			.catch(function(){	
				Message.alert("There was an error");
			})
			.finally(function(){			
			});
		} else {
			controller.setSearchParameters();			
			if (!angular.equals({}, controller.obtainSearchParameters())){
				controller.search();
			}
		}
		
		
		

		$scope.selectHierarchy = function(hierarchy){
			$scope.hierarchy = hierarchy;
			//controller.setSearchParameters();
			controller.updateSearch(true);
		}
		
		

		
		
		/*//Removed due to product list controller having a partial model of the product objects
		 
		this.update = function(product) {			
			
			Product.modify(product)
			.success(function(data){
				product = angular.copy(data);
			})
			.catch(function(data){
				Message.alert("There was an error");
			})
			.finally(function(data){
				
			});
		};
		*/
		
		this.remove = function(product){							
			Product.remove(product)			
			.success(function(data){				
				if ($scope.products){							
					$scope.products = $scope.products.filter(function(e){ return e.id != data});					
				}			
			})
			.catch(function(data){		
				Message.alert("There was an error");
			})
			.finally(function(){ 				
			});
		}

		this.calculateUrl = function(){
			var url = '/products/';
			
			if ($scope.root.id!=null && $scope.hierarchy.id!=null) {
				url = url + $scope.hierarchy.id + '/';				
			}
			
			return url;
		};
		
		this.nextPage = function(){
			if ($scope.page == null || $scope.page == undefined || isNaN(parseInt($scope.page))){
				$scope.page = 0;
			}
			$scope.page = parseInt($scope.page);
			$scope.page = parseInt($scope.page) +1;
			controller.updateSearch();
			
			if ($scope.positionInitialGrid){
				$scope.positionInitialGrid();
			}
		}
		
		this.previousPage = function(){
			if ($scope.page == null || $scope.page == undefined || isNaN(parseInt($scope.page))){
				$scope.page = 0;
			}
			if ($scope.page>0){
				$scope.page = parseInt($scope.page) -1;
				controller.updateSearch();
			}
			if ($scope.positionInitialGrid){
				$scope.positionInitialGrid();
			}
		}
		
		this.hasNext = function(){
			var result = false;
			if ($scope.products!=null && $scope.products.length >0 && ($scope.hasNext == true)){
				result = true;
			}			
			return result;
		};
		
		this.hasPrevious = function(){
			var result = true;
			if ($scope.page  && $scope.page > 0){
				result = true;
			} else {
				result = false;
			}
			return result;
		};
		
		this.toggleEditMode = function(){
			if ($scope.editMode) {
				$scope.editMode = false;
			} else {
				$scope.editMode = true;				
			}			
		}
		
	}]);
	
})();