(function(){
	
	angular.module('product')
	.controller('ProductDetailsController',['$routeParams','$scope','Product','Image','Message',
	                                           function($routeParams,$scope,Product,Image,Message){
		var controller = this;
		
		$scope.modifyLite = function(product){
			Product.modifyLite(product)
			.success(function(data){
				product = angular.copy(data);
			})
			.catch(function(data){
				Message.alert("There was an error");
			})
			.finally(function(data){
				
			});
		}
		
		$scope.modifyImageLite = function(image){
			Image.modifyLite(image)
			.success(function(data){
				if (data && data.id){
					if ($scope.product && $scope.product.selectedImage && data.id == $scope.product.selectedImage.id){
						$scope.product.selectedImage = data;
					}
					if ($scope.product && $scope.product.images && $scope.product.images.length){
						for (var i=0;i<$scopeproduct.images.length;i++){
							var image = product.images[i];
							if (image.id && image.id == data.id){
								product.images[i] = data;
							}
						}
					}
				}
			})
			.catch(function(data){
				Message.alert("There was an error");
			})
			.finally(function(data){
				
			});
			
		}
		
		this.pullProduct = function(id){
			Product.one(id)
			.success(function(data){
				$scope.product = Product.prepareProduct(data);
			})
			.catch(function(){	
				Message.alert("There was an error");
			})
			.finally(function(){			
			});
		}
		
		this.updatePrices = function(id){
			$scope.updatingPrice = true;
			Product.updatePrice(id)
			.success(function(data){			
				$scope.product = Product.prepareProduct(data);				
			})
			.catch(function(){	
				Message.alert("There was an error");
			})
			.finally(function(){		
				$scope.updatingPrice = false;
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

		this.setProduct = function(product){					
			angular.copy(product,$scope.product);
			angular.copy($scope.product,controller.product);			
		};
		
		this.setSelectedImage = function(image){			
			$scope.product.selectedImage = image;
		}
			
		
		$scope.newProductAvailable = false;		
		
		$scope.isEmptyObject = function(obj){	    	
	    	return angular.equals({}, obj);
	    };
		
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
			controller.pullProduct($routeParams.id);
		}
		
	}]);
	
	
})();