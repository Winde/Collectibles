(function(){
	
	angular.module('product')
	.controller('ProductChangeController',['$scope','Hierarchy','Product','Message',
	                                          function($scope,Hierarchy,Product,Message){
		
		var controller = this;

		$scope.hierarchy = {};
		$scope.hierarchies = [];
		controller.uploading = false;
		
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
		
		this.refreshScrape = function(){
			if ($scope.product!=null && $scope.product.id!=null){
				Product.refresh($scope.product)
				.success(function(data){					
					$scope.product = Product.prepareProduct(data);					
					Message.success("Products has been refreshed");			
				})
				.catch(function(){
					Message.alert("There was an error");
				})
				.finally(function(){
					
				})
			}			
		}
		
		this.uploadFile = function(){			
			if (controller.uploading!=true){
				controller.uploading = true;
				Product.uploadFile($scope.hierarchy,$scope.file)
				.success(function(data){					
					$scope.file = [];					 				
					Message.success("Products have been uploaded",true);					
				})
				.catch(function(){
					Message.alert("There was an error",true);
				})
				.finally(function(){
					controller.uploading = false;
				});
			} else {
				Message.alert("Upload in progress",false);
			}
		};
		
		this.changeProduct = function(){
			var product = $scope.product;
			
			if (product.hierarchyPlacement!=null && product.hierarchyPlacement!=undefined && 
				product.hierarchyPlacement.id!=null && product.hierarchyPlacement.id!=undefined){					
				
				var productToSend = angular.copy(product);
				
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
					$scope.product = Product.prepareProduct(data);
					
				})
				.catch(function(){					
					Message.alert("There was an error");
				})
				.finally(function(){					
				});
			} 
		};
		
		this.uploadImage = function(file){
			
			var product = $scope.product;
			
			Product.addImage(product,file)
			.success(function(data){
				if ((product.images== null || product.images.length==0) && data.length>0) {
					product.selectedImage = data[0];
				}
				if (product.images){
					product.images = product.images.concat(data);
				} else {
					product.images = data;
				}
				
			})
			.catch(function(){
				Message.alert("There was an error");
			})
			.finally(function(){
				
			});
			
		}
	}]);
	
})();