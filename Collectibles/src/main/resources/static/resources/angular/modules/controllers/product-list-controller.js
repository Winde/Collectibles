(function(){
	
	angular.module('product')
	.controller('ProductListController',['$scope','$filter','$location','Image','Product','Hierarchy','Message',
	                                        function($scope,$filter,$location,Image,Product,Hierarchy,Message){
		var controller = this;
				
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
		
		this.updateSearch = function(){
			var searchObject = {};
			
			console.log($scope.owned);
			
			if ($scope.hierarchy && $scope.hierarchy.id){ searchObject.hierarchy = $scope.hierarchy.id;}			
			
			if ($scope.searchTerm){ searchObject.search = $scope.searchTerm; }			
			
			if ($scope.withImages){ searchObject.withImages = $scope.withImages; }
			
			if ($scope.owned){ searchObject.owned = $scope.owned}
			
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
			
		};
				
				
		this.search = function() {
			
			
			var searchTerm = $scope.searchTerm;
			var withImages = null;
			if ($scope.withImages == 'true' || $scope.withImages == 'false'){
				withImages = $scope.withImages;
			}
			var owned = null;
			if ($scope.owned == 'true' || $scope.owned == 'false'){
				owned = $scope.owned;
			}			
			
			console.log($scope.root);
			console.log($scope.hierarchy);
			console.log(searchTerm);
			
			if ( ($scope.hierarchy!=null && $scope.hierarchy.id) 
					|| (searchTerm && searchTerm!="" && searchTerm.length>2) ){				
				Product.search($scope.hierarchy,searchTerm,withImages,owned)
				.success(function(data){ 
					$scope.products = data;					
				})
				.catch(function(data){
					Message.alert("There was an error");
					$scope.products = [];				
				}).finally(function(data){					
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
		
	}]);
	
})();