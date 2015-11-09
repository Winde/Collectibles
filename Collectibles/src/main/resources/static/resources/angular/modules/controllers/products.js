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
	
	app.controller('ProductListController',['$scope','$filter','$location','Image','Product','Hierarchy','Message',
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

			console.log("Hierarchy: " + searchObject.hierarchy);
			console.log("Hierarchy: " + parseInt(searchObject.hierarchy));
			
			
			
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
			
			console.log("BEFORE SEARCH: ");
			console.log($scope.hierarchy);
			
			var searchTerm = $scope.searchTerm;
			var withImages = null;
			if ($scope.withImages == 'true' || $scope.withImages == 'false'){
				withImages = $scope.withImages;
			}
			var owned = null;
			if ($scope.owned == 'true' || $scope.owned == 'false'){
				owned = $scope.owned;
			}			
			
			if (	
					($scope.root==undefined 
							|| $scope.root.id == undefined
							|| $scope.hierarchy == undefined 
							|| $scope.hierarchy.id == undefined
							|| $scope.root.id!=$scope.hierarchy.id) 
					|| (searchTerm!=null && searchTerm !=undefined && searchTerm!="")
				){				
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
		
	app.controller('ProductChangeController',['$scope','Hierarchy','Product','Message',
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