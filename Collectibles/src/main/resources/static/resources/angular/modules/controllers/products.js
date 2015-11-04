(function(){
	
	var app = angular.module('products-controllers',[]);
	
	
	app.controller('ProductDetailsController',['$routeParams','$scope','Product','Image','Message',
	                                           function($routeParams,$scope,Product,Image,Message){
		var controller = this;
		this.product = $scope.product;					
		
		$scope.newProductAvailable = false;
	
		this.setProduct = function(product){
			controller.product = product;	
			angular.copy(product, $scope.product);
			console.log($scope.product);
		};
		
		if ($routeParams.id){
			Product.one($routeParams.id)
			.success(function(data){
				controller.setProduct(data);			
				if (data.images!=undefined && data.images!=null && data.images.length>0){				
					Image.multiple(data.images)
					.success(function(data){
						controller.product.images = data;										
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
				controller.product.images = controller.product.images.filter(function(e){ return e.id != data}); 
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
		
		controller.root = {}
		controller.products = [];
		controller.hierarchies = [];
		controller.hierarchy = {};
		controller.searchTerm = "";
		controller.ajax = false;
				
		Hierarchy.root()
		.success(function(data){
			controller.root = { id: data.id };
			controller.hierarchy = { id: data.id };
			controller.hierarchies = [{ id: data.id, name: "All"}];			
			if (data.children!=null && data.children!=undefined && data.children.length>0){
				controller.hierarchies = controller.hierarchies.concat(data.children);
			}							
		})
		.catch(function(){	
			Message.alert("There was an error");
		})
		.finally(function(){			
		});
		
		this.remove = function(product){			
			controller.ajax = true;		
			Product.remove(product)			
			.success(function(data){				
				if ($scope.productListCtrl && $scope.productListCtrl.products){												
					$scope.productListCtrl.products = $scope.productListCtrl.products.filter(function(e){ return e.id != data});
				}				
			})
			.catch(function(data){				
			})
			.finally(function(){ 
				controller.ajax = false; 
			});
		}

		this.search = function() {
			
			var searchTerm = controller.searchTerm; 
			if (	
					(controller.root.id!=controller.hierarchy.id) || 
					(searchTerm!=null && searchTerm !=undefined && searchTerm!="")
				){
				controller.ajax = true;
				Product.search(controller.root,controller.hierarchy,searchTerm)
				.success(function(data){ 
					controller.products = data; 				
				})
				.catch(function(data){
					Message.alert("There was an error");
					controller.products = [];				
				}).finally(function(data){
					controller.ajax = false;
				});
			} else {
				controller.products = [];
			}
		}
		
	}]);
		
	app.controller('ProductChangeController',['$scope','Hierarchy','Product','Message',
	                                          function($scope,Hierarchy,Product,Message){
		var controller = this;

		this.product = $scope.product;	
		this.createnew = $scope.createnew;
		
		this.hierarchy = {};
		this.hierarchies = [];
		
		Hierarchy.root()
		.success(function(data){
			controller.hierarchies = data.children;			
		})
		.catch(function(){		
			Message.alert("There was an error");
		})
		.finally(function(){			
		});
		
		
		this.canCreateNew = function(){						
			return (controller.createnew != "false" && controller.createnew!= false);
		}
		
		this.newProduct = function(){
			this.product = {};
		}
		
		this.changeProduct = function(){
			var product = controller.product;
			
			if (product.hierarchyPlacement!=null && product.hierarchyPlacement!=undefined && 
				product.hierarchyPlacement.id!=null && product.hierarchyPlacement.id!=undefined){					
				
				var productToSend = angular.copy(product);
				productToSend.images = null;
				
				var promise = null;
				if (product.id!=null && product.id!=undefined){
					productSubmission = Product.create(productToSend);
				} else {
					productSubmission = Product.modify(productToSend);
				}
				
				productSubmission
				.success(function(data) {
					if (product.id==null){
						Message.success("Product was created");
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