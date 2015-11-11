(function(){
	
	angular.module('product')
	.controller('ProductListController',['$scope','$filter','$location','Image','Product','Hierarchy','Message',
	                                        function($scope,$filter,$location,Image,Product,Hierarchy,Message){
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
			
			if ($scope.owned){ searchObject.owned = $scope.owned}
			
			if ($scope.page){ searchObject.page = $scope.page } else { $scope.page = 0; searchObject.page = 0; }
			
			if ($scope.maxResults){ searchObject.maxResults = $scope.maxResults } else { $scope.maxResults = 0; searchObject.maxResults = 0; }
			
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

			if (searchObject.hierarchy){
				var hierarchy = parseInt(searchObject.hierarchy)
				if (!isNaN(hierarchy)){
					$scope.hierarchy = {id: hierarchy};
				}
			} else {	$scope.hierarchy = {};}
			
			if (searchObject.search){		$scope.searchTerm = searchObject.search;
			} else {	$scope.searchTerm = "";}
			
			if (searchObject.withImages){	$scope.withImages = searchObject.withImages;
			} else {	$scope.withImages = "";}
			
			if (searchObject.owned){	$scope.owned = searchObject.owned;
			} else {	$scope.owned = "";}
			
			if (searchObject.page){	$scope.page = searchObject.page;
			} else {	searchObject.page = 0; $scope.page= 0; }
			
			if (searchObject.maxResults){	$scope.maxResults = searchObject.maxResults;
			} else {	searchObject.maxResults = controller.defaultPaginationSize; $scope.maxResults= controller.defaultPaginationSize; }
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
		
		
		
		
		if ($scope.root == undefined || $scope.root == null){
			Hierarchy.root()
			.success(function(data){
				$scope.root = { id: data.id };				
				if ($scope.hierarchy == null || $scope.hierarchy == undefined){
					$scope.hierarchy = { id: data.id };
				}				
				$scope.hierarchies.push({ id: data.id, name: "All", isRoot: true});			
				Hierarchy.calculateTree(data,$scope.hierarchies);
				/**/
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
		}
		
		this.previousPage = function(){
			if ($scope.page == null || $scope.page == undefined || isNaN(parseInt($scope.page))){
				$scope.page = 0;
			}
			if ($scope.page>0){
				$scope.page = parseInt($scope.page) -1;
				controller.updateSearch();
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