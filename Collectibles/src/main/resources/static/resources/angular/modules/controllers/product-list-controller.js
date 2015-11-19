(function(){
	
	angular.module('product')
	.controller('ProductListController',['$scope','$filter','$location','Image','Product','Hierarchy','User','Message',
	                                        function($scope,$filter,$location,Image,Product,Hierarchy,User,Message){
		var controller = this;
				
		$scope.editMode = false;
		
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

			if ($scope.hierarchy && $scope.hierarchy.id){ searchObject.hierarchy = $scope.hierarchy.id;}			
			
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
			controller.search();
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
			

		};
				
				
		this.search = function() {
			var searchObject = controller.obtainSearchParameters();
		
			if (searchObject.hierarchy || (searchObject.searchTerm && searchObject.searchTerm!="" && searchObject.searchTerm.length>2) ){
				$scope.processingSearch = true;
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
		
		if ($scope.root == undefined || $scope.root == null){
			Hierarchy.root()
			.success(function(data){				
				$scope.root = { id: data.id };				
				if ($scope.hierarchy == null || $scope.hierarchy == undefined){
					$scope.hierarchy = { id: data.id };
				}				
				var all = { id: data.id, name: "All", isRoot: true};
				$scope.hierarchies.push(all);			
				$scope.allHierarchies = [];
				Hierarchy.calculateTree(data,$scope.hierarchies,3);
				Hierarchy.calculateTree(data,$scope.allHierarchies);
				
				$scope.hierarchy = all;				
				controller.setSearchParameters();	
								
				
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
		},
		
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
			
			setTimeout(function () {
		        window.scrollTo(0, $('.productGrid') - 100)
		    }, 20);
		}
		
		this.previousPage = function(){
			if ($scope.page == null || $scope.page == undefined || isNaN(parseInt($scope.page))){
				$scope.page = 0;
			}
			if ($scope.page>0){
				$scope.page = parseInt($scope.page) -1;
				controller.updateSearch();
			}
			setTimeout(function () {
		        window.scrollTo(0, $('.productGrid') - 100)
		    }, 20);
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