(function(){
	
	angular.module('product')
	.controller('ProductDetailsController',['$routeParams','$scope','Product','Image','Message',
	                                           function($routeParams,$scope,Product,Image,Message){
		var controller = this;
		
		$scope.newProductAvailable = false;
	
		this.setProduct = function(product){					
			angular.copy(product,$scope.product);
			angular.copy($scope.product,controller.product);			
		};
		
		this.setSelectedImage = function(image){			
			$scope.product.selectedImage = image;
		}
				
		if ($scope.product && $scope.product.images){
			var image = null;
			if ($scope.product.images && $scope.product.images.length){
				for (var i=0;i<$scope.product.images.length;i++){
					if ($scope.product.images[i].main == true){
						image = $scope.product.images[i];
					}
				}
				if (image == null){
					image = $scope.product.image[0];
				}
				$scope.product.selectedImage = image;
			}			
		}
		
		
		if ($routeParams.id){
			Product.one($routeParams.id)
			.success(function(data){
				controller.setProduct(data);			
				if (data.images!=undefined && data.images!=null && data.images.length>0){
					if ($scope.product.mainImage != null) {
						for (var i=0;i<data.images.length;i++){
							if (data.images[i].id == $scope.product.mainImage.id){
								$scope.product.selectedImage =data.images[i]; 
							}
						}
					} else {
						$scope.product.selectedImage =data.images[0]; 
					}										
				}
			})
			.catch(function(){	
				Message.alert("There was an error");
			})
			.finally(function(){			
			});
		}
		this.removeImage = function(image){
			
			Product.removeImage($scope.product,image)
			.success(function(data){								
				$scope.product.images = $scope.product.images.filter(function(e){ return e.id != data});
				if ($scope.product.selectedImage.id == image.id) {					
					if ($scope.product.images.length>0){						
						$scope.product.selectedImage = $scope.product.images[0];
					} else{
						$scope.product.selectedImage = null;
					}
				}				
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
	
	
})();