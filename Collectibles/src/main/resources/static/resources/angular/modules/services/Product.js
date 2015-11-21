(function(){
	
	angular.module('product')
	.factory('Product', function ProductFactory($http,Upload,$httpParamSerializer){
		var factory = this;
		
		this.cleanUpProduct = function cleanUpProduct(product){
			if (product.universalReference == ""){
				product.universalReference = null;
			}
		}
		
		return {		
			one: function(id) {				
				return $http({ 
					method: 'GET', 
					url: '/product/find/'+id, 
					progressbar: true 
				});
			},
			refresh: function(product) {				
				return $http({ 
					method: 'PUT', 
					url: '/product/refresh/', 
					data: product,
					progressbar: true 
				});
			},
			create: function(product){
				factory.cleanUpProduct(product);
				return $http({ 
					method: 'POST', 
					url: '/product/create/'+product.hierarchyPlacement.id, 
					data: product,
					progressbar: true
				});				
			},
			modify: function(product){
				factory.cleanUpProduct(product);
				return $http({ 
					method: 'PUT', 
					url: '/product/modify/', 
					data: product,
					progressbar: true
				});
			},
			remove: function(product){
				return $http.post('/product/remove/'+product.id,{
					progressbar: true
				});
			},
			updatePrice: function(id){
				return $http({ 
					method: 'PUT', 
					url: '/product/update/price/'+id, 
					progressbar: true 
				});
			},
			search: function(searchObject){				
				var url = "/product/search/";

				if ( searchObject && searchObject.hierarchy){
					url = url + searchObject.hierarchy + "/";
				}
				return $http.get(url,{
					progressbar: true,
					params: searchObject
				});
			},
			updatePricesForSearch: function(searchObject){
				var url = "/product/update/prices/";

				if ( searchObject && searchObject.hierarchy){
					url = url + searchObject.hierarchy + "/";
				}
				return $http({
					url: url,
					method: 'POST',
					progressbar: true,
					data: $httpParamSerializer(searchObject),		
					headers: {
					    'Content-Type': 'application/x-www-form-urlencoded'
					}
				});
			},
			addImage: function(product,file){
				var upload = Upload.upload({
					url: '/product/'+product.id+'/image/add/',
					data: { images: file },
					progressbar: true
				});
				return upload;
			},
			removeImage: function(product,image){
				return $http({
					method: 'POST', 
					url: '/product/'+product.id+'/image/remove/'+image.id,
					progressbar: true
				});
			},
			uploadFile: function(hierarchy,file){
				var upload = Upload.upload({
		            url: '/product/create/from/file/'+hierarchy.id,
		            data: {file: file},
		            progressbar: true
				});
								
				return upload;
			},
			prepareProduct : function(data){							
				if (data.images!=undefined && data.images!=null && data.images.length>0){
					if (data.mainImage != null) {
						for (var i=0;i<data.images.length;i++){
							if (data.images[i].id == data.mainImage.id){
								data.selectedImage =data.images[i]; 
							}
						}
					} else {
						data.selectedImage =data.images[0]; 
					}										
				}
				
				if (data.hierarchyPlacement) {
					data.hierarchyPath = [];
					var current = data.hierarchyPlacement; 										
					while (current.father){
						data.hierarchyPath.unshift(current);
						current = current.father;
					}
					
				}
				return data;
			} 			
		}		
	});
	
	
})();