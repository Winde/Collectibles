(function(){
	
	var app = angular.module('products-controllers',[]);
	
	
	app.controller('ProductDetailsController',['$routeParams','$scope','Product','Image','Message',
	                                           function($routeParams,$scope,Product,Image,Message){
		var controller = this;
		
		$scope.newProductAvailable = false;
	
		this.setProduct = function(product){					
			angular.copy(product,$scope.product);
			angular.copy($scope.product,controller.product);			
		};
		
		if ($routeParams.id){
			Product.one($routeParams.id)
			.success(function(data){
				controller.setProduct(data);			
				if (data.images!=undefined && data.images!=null && data.images.length>0){				
					Image.multiple(data.images)
					.success(function(data){
						$scope.product.images = data;										
					})
					.catch(function(){		
						Message.alert("There was an error");
					})
					.finally(function(){					
					});
				}
			})
			.catch(function(){	
				Message.alert("There was an error");
			})
			.finally(function(){			
			});
		}
		this.remove = function(image){
			
			Product.removeImage(controller.product,image)
			.success(function(data){
				$scope.product.images = $scope.product.images.filter(function(e){ return e.id != data}); 
			})
			.catch(function(){	
				Message.alert("There was an error");
			})
			.finally(function(){				
			});
		};
		
		this.setEditable = function(value){
			$scope.editable = value;
		};
		
		this.isEditable = function(){	
			return ($scope.editable === true);
		};				
		this.isEditForbidden = function(){
			return ($scope.forbidedit == true);
		};
		
	}]);
	
	app.controller('ProductListController',['$scope','$filter','Product','Hierarchy','Message',
	                                        function($scope,$filter,Product,Hierarchy,Message){
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
		
		$scope.hierarchy = {};
		$scope.searchTerm = "";
		$scope.ajax = false;
			
		if ($scope.root == undefined || $scope.root == null){
			Hierarchy.root()
			.success(function(data){
				$scope.root = { id: data.id };
				$scope.hierarchy = { id: data.id };
				$scope.hierarchies.push({ id: data.id, name: "All", isRoot: true});			
				Hierarchy.calculateTree(data,$scope.hierarchies);			
			})
			.catch(function(){	
				Message.alert("There was an error");
			})
			.finally(function(){			
			});
		}
		this.remove = function(product){			
			$scope.ajax = true;		
			Product.remove(product)			
			.success(function(data){				
				if ($scope.$parent.products){					
					$scope.$parent.products = $scope.$parent.products.filter(function(e){ return e.id != data});					
				}				
			})
			.catch(function(data){				
			})
			.finally(function(){ 
				$scope.ajax = false; 
			});
		}

		this.search = function() {
			
			var searchTerm = $scope.searchTerm; 
			if (	
					($scope.root.id!=$scope.hierarchy.id) || 
					(searchTerm!=null && searchTerm !=undefined && searchTerm!="")
				){
				$scope.ajax = true;
				Product.search($scope.root,$scope.hierarchy,searchTerm)
				.success(function(data){ 
					$scope.products = data; 				
				})
				.catch(function(data){
					Message.alert("There was an error");
					$scope.products = [];				
				}).finally(function(data){
					$scope.ajax = false;
				});
			} else {
				$scope.products = [];
			}
		}
		
	}]);
		
	app.controller('ProductChangeController',['$scope','Hierarchy','Product','Message',
	                                          function($scope,Hierarchy,Product,Message){
		

		$scope.hierarchy = {};
		$scope.hierarchies = [];
		
		Hierarchy.root()
		.success(function(data){
			Hierarchy.calculateTree(data,$scope.hierarchies);			
		})
		.catch(function(){		
			Message.alert("There was an error");
		})
		.finally(function(){			
		});
		
		
		this.canCreateNew = function(){						
			return ($scope.createnew != "false" && $scope.createnew!= false);
		}
		
		this.newProduct = function(){
			$scope.product = {};
		}
		
		this.uploadFile = function(){
			
			Product.uploadFile($scope.hierarchy,$scope.file)
			.success(function(data){
				Message.success("Products have been uploaded",true);
			})
			.catch(function(){
				Message.alert("There was an error",true);
			})
			.finally(function(){
				
			});
		};
		
		this.changeProduct = function(){
			var product = $scope.product;
			
			if (product.hierarchyPlacement!=null && product.hierarchyPlacement!=undefined && 
				product.hierarchyPlacement.id!=null && product.hierarchyPlacement.id!=undefined){					
				
				var productToSend = angular.copy(product);
				productToSend.images = null;
				
				var promise = null;				
				if (product.id==null || product.id==undefined){					
					productSubmission = Product.create(productToSend);
				} else {					
					productSubmission = Product.modify(productToSend);
				}
				
				productSubmission
				.success(function(data) {
					if (product.id==null){
						Message.success("Product was created",true);
					} else {
						Message.success("Product was updated");
					}
					product.id = data.id;	
					
				})
				.catch(function(){					
					Message.alert("There was an error");
				})
				.finally(function(){					
				});
			} 
		}
	}]);
	
})();