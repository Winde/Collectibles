(function(){
	
	angular.module('products-services',[])
	.factory('Product', function ProductFactory($http){
		return {
			
			one: function(id) {
				return $http({ method: 'GET', url: '/product/find/'+id });
			},			
			create: function(product){
				return $http({ method: 'POST', url: '/product/create/'+product.hierarchyPlacement.id, data: product });				
			},
			remove: function(product){
				return $http.post('/product/remove/'+product.id);
			},
			search: function(root,hierarchy,searchTerm){				
				var url = "/product/search/";
												
				if (
						hierarchy!=null &&
						hierarchy.id!=null &&
						hierarchy.id!=root.id	
				){
					url = url + hierarchy.id + "/";
				}
								
				if (searchTerm!=null && searchTerm!=undefined && searchTerm!=""){
					url = url + '?search='+searchTerm;				
				} 					
				
				return $http.get(url);
			},
			modify: function(product){
				return $http({ method: 'PUT', url: '/product/modify/', data: product });
			},
			removeImage: function(product,image){
				return $http({method: 'POST', url: '/product/'+product.id+'/image/remove/'+image.id});
			}
			
		}		
	});
	
	
})();