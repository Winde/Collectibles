(function(){
	
	var app = angular.module('products-controllers',[]);
	
	
	app.controller('ProductDetailsController',['$http','$routeParams','$scope', function($http,$routeParams,$scope){
		var controller = this;
		this.product = {};
		this.editable = false;
		
		$scope.newProductAvailable = false;
		
		this.setProduct = function(product){
			this.product = product;
			$scope.product = product;
		};
		
		$http({ method: 'GET', url: '/product/find/'+$routeParams.id })
		.success(function(data){
			controller.setProduct(data);			
			if (data.images!=undefined && data.images!=null && data.images.length>0){
				var parameter = "";
				var i=0;
				for (var i=0;i<data.images.length; i++){
					var image = data.images[i];					
					parameter = parameter + image.id					
					if (i<data.images.length-1){
						parameter = parameter + ",";
					}
				}				
				$http({method: 'GET', url: '/images/'+parameter}).success(function(data){
					controller.product.images = data;										
				});
			}
		})
		.catch(function(){
			
		});
		
		this.remove = function(image){
			
			$http({method: 'POST', url: '/product/'+controller.product.id+'/image/remove/'+image.id})
			.success(function(data){
				controller.product.images = controller.product.images.filter(function(e){ return e.id != data}); 
			});			
		};
		
		this.setEditable = function(value){
			controller.editable = value;
		};
		
		this.isEditable = function(){
			return this.editable === true;
		};
		
		this.edit = function(){
			$http({method: 'POST', url: '/product/'+controller.product.id+'/image/remove/'+image.id})
		};
		
	}]);
	
	app.controller('ProductListController',['$scope','$http','$filter', function($scope,$http,$filter){
		var controller = this;
		
		controller.root = {}
		controller.products = [];
		controller.hierarchies = [];
		controller.hierarchy = {};
		controller.searchTerm = "";
		controller.ajax = false;
				
		$http.get('/hierarchy/root/')
		.success(function(data){
			controller.root = { id: data.id };
			controller.hierarchy = { id: data.id };
			controller.hierarchies = [{ id: data.id, name: "All"}];			
			if (data.children!=null && data.children!=undefined && data.children.length>0){
				controller.hierarchies = controller.hierarchies.concat(data.children);
			}							
		});
		
		this.remove = function(product){			
			controller.ajax = true;			
			$http.post('/product/remove/'+product.id)
			.success(function(data){				
				if ($scope.productListCtrl && $scope.productListCtrl.products){												
					$scope.productListCtrl.products = $scope.productListCtrl.products.filter(function(e){ return e.id != data});
				}				
			})
			.error(function(data){				
			})
			.finally(function(){ 
				controller.ajax = false; 
			});
		}

		this.search = function() {
			controller.ajax = true;			
			var searchTerm = controller.searchTerm; 
							
			var url = "/product/search/";
											
			if (controller.hierarchy!=null &&
				controller.hierarchy.id!=null &&
				controller.hierarchy.id!=controller.root.id	
			){
				url = url + controller.hierarchy.id + "/";
			}
							
			if (searchTerm!=null && searchTerm!=undefined && searchTerm!=""){
				url = url + '?search='+searchTerm;				
			} 					
			
			$http.get(url)
				.success(function(data){ 
					controller.products = data; 				
				})
				.error(function(data){
					controller.products = [];				
				}).finally(function(data){
					controller.ajax = false;
				});
		}
		
	}]);
		
	app.controller('ProductChangeController',['$http','$log','$scope',function($http,$log,$scope){
		var controller = this;
				
		this.product = $scope.product;				
		this.hierarchy = {};
		this.hierarchies = [];
		
		$http.get('/hierarchy/root/')
			.success(function(data){
				controller.hierarchies = data.children;			
			})
			.error(function(){				
			});
		
		
		this.canCreateNew = function(){			
			$scope.newProductAvailable != false;
		}
		
		this.newProduct = function(){
			this.product = {};
		}
		
		this.changeProduct = function(){
			var product = controller.product;
			
			if (product.hierarchyPlacement!=null && product.hierarchyPlacement!=undefined && 
				product.hierarchyPlacement.id!=null && product.hierarchyPlacement.id!=undefined){					
				
				var method = 'POST';
				var url = "";
				if (product.id!=null && product.id!=undefined){
					url = '/product/modify/';
					method = 'PUT';
				} else {
					method = 'POST';
					url = '/product/create/'+product.hierarchyPlacement.id;
				}
				
				
				var productToSend = angular.copy(product);
				productToSend.images = null;
				$http({
					method: method, 
					url: url,
					data: productToSend					
				})
					.success(function(data) {
						product.id = data.id;				
					})
					.error(function(data,e,i){					
						alert(JSON.stringify(data));
					});
			} 
		}
	}]);
	
})();