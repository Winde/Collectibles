(function(){
	
	var app = angular.module('products-controllers',[]);

	app.controller('ProductListController',['$scope','$http','$filter', function($scope,$http,$filter){
		var productListController = this;
		
		var root = {}
		var products = [];
		var hierarchies = [];
		var hierarchy = {};
		var searchTerm = "";
		var ajax = true;
		
		var productsPromise = $http.get('/product/search/1/');
		var hierarchiesPromise = $http.get('/hierarchy/root/');
		
		hierarchiesPromise.success(function(data){
			productListController.root = { id: data.id };
			productListController.hierarchy = { id: data.id };
			productListController.hierarchies = [{ id: data.id, name: "All"}];
			console.log(JSON.stringify(data.children));
			if (data.children!=null && data.children!=undefined && data.children.length>0){
				productListController.hierarchies = productListController.hierarchies.concat(data.children);
			}
			console.log(JSON.stringify(productListController.hierarchies));					
		});
		
		this.remove = function(product){			
			productListController.ajax = true;
			var productsPromise = null;
			productsPromise = $http.post('/product/remove/'+product.id);
			
			console.log($scope);
			productsPromise.success(function(data){				
				if ($scope.productListCtrl && $scope.productListCtrl.products){												
					$scope.productListCtrl.products = $scope.productListCtrl.products.filter(function(e){ return e.id != data});
				}				
			});
			
			productsPromise.error(function(data){				
			});
			productsPromise.finally(function(){ productListController.ajax = false; });
		}

		this.search = function() {
			productListController.ajax = true;
			var productsPromise = null;
			var searchTerm = productListController.searchTerm; 
							
			var url = "/product/search/";
											
			if (productListController.hierarchy!=null &&
				productListController.hierarchy.id!=null &&
				productListController.hierarchy.id!=productListController.root.id	
			){
				url = url + productListController.hierarchy.id + "/";
			}
							
			if (searchTerm!=null && searchTerm!=undefined && searchTerm!=""){
				url = url + '?search='+searchTerm;				
			} 		
			console.log(url);
			productsPromise = $http.get(url);
			productsPromise.success(function(data){ 
				productListController.products = data; 				
			});
			
			productsPromise.error(function(data){
				productListController.products = [];				
			});
			
			productsPromise.finally(function(data){
				productListController.ajax = false;
			});
		}
		
	}]);
		
	app.controller('ProductCreationController',['$http','$log',function($http,$log){
		var productCreationController = this;
		
		this.product = {};
		this.hierarchy = {};
		this.hierarchies = [];
		
		var hierarchiesPromise = $http.get('/hierarchy/root/');
		
		hierarchiesPromise.success(function(data){
			productCreationController.hierarchies = data.children;			
		});
		
		hierarchiesPromise.error(function(){
			$log('error');
		});
			
		this.addProduct = function(){
			var product = productCreationController.product;				
			if (product.hierarchyNode!=null && product.hierarchyNode!=undefined && 
				product.hierarchyNode.id!=null && product.hierarchyNode.id!=undefined){	
				console.log("entered");
				var productAddPromise = $http({
					method: 'POST', 
					url: '/product/create/'+product.hierarchyNode.id,
					data: product					
				});
				productAddPromise.success(function(data) {
					productCreationController.product = { hierarchyNode : product.hierarchyNode};				
				});			
				productAddPromise.error(function(data,e,i){					
					alert(JSON.stringify(data));
				})
			} else {
				console.log("not entered");
			}
		}
	}]);
	
})();