(function(){
	
	angular.module('product')
	.factory('Product', function ProductFactory(Domain,$http,Upload,$httpParamSerializer){
		var factory = this;
		
		this.cleanUpProduct = function cleanUpProduct(product){
			if (product.universalReference == ""){
				product.universalReference = null;
			}
		}
		
		return {		
			one: function(id) {				
				return $http({ 					
					url: Domain.base()+'/product/find/'+id,
					method: 'GET', 
					progressbar: true 
				});
			},
			refresh: function(product) {				
				return $http({ 
					method: 'PUT', 
					url: Domain.base()+'/product/refresh/', 
					data: product,
					progressbar: true 
				});
			},
			create: function(product){
				factory.cleanUpProduct(product);
				return $http({ 
					method: 'POST', 
					url: Domain.base()+'/product/create/'+product.hierarchyPlacement.id, 
					data: product,
					progressbar: true
				});				
			},
			modifyLite: function(product){
				return $http({ 
					method: 'PUT', 
					url: Domain.base()+'/product/modify/minor/', 
					data: product,
					progressbar: true
				});
			},			
			modify: function(product){
				factory.cleanUpProduct(product);
				return $http({ 
					method: 'PUT', 
					url: Domain.base()+'/product/modify/', 
					data: product,
					progressbar: true
				});
			},
			remove: function(product){
				return $http.post(Domain.base()+'/product/remove/'+product.id,{
					progressbar: true
				});
			},
			updatePrice: function(id){
				return $http({ 
					method: 'PUT', 
					url: Domain.base()+'/product/update/price/'+id, 
					progressbar: true 
				});
			},
			search: function(searchObject){				
				var url = Domain.base()+"/product/search/";

				if ( searchObject && searchObject.hierarchy){
					url = url + searchObject.hierarchy + "/";
				}
				return $http.get(url,{
					progressbar: true,
					params: searchObject
				});
			},
			updatePricesForSearch: function(searchObject){
				var url = Domain.base()+"/product/update/prices/";

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
					url: Domain.base()+'/product/'+product.id+'/image/add/',
					data: { images: file },
					progressbar: true
				});
				return upload;
			},
			removeImage: function(product,image){
				return $http({
					method: 'POST', 
					url: Domain.base()+'/product/'+product.id+'/image/remove/'+image.id,
					progressbar: true
				});
			},
			uploadFile: function(hierarchy,file){
				var upload = Upload.upload({
		            url: Domain.base()+'/product/create/from/file/'+hierarchy.id,
		            data: {file: file},
		            progressbar: true
				});
								
				return upload;
			},
			importFromScrapUser: function(connector,userId,hierarchyId){
				
				return $http({
					method: 'POST', 
					url: Domain.base()+'/product/create/from/'+connector+'/user/'+userId+'/tohierarchy/'+hierarchyId,
					progressbar: true
				});
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