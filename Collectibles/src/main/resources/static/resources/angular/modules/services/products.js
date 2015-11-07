(function(){
	
	angular.module('products-services',['ngFileUpload'])
	.factory('Product', function ProductFactory($http,Upload){
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
			search: function(root,hierarchy,searchTerm,withImages){				
				var url = "/product/search/";
												
				if (
						hierarchy!=null &&
						hierarchy.id!=null &&
						hierarchy.id!=root.id	
				){
					url = url + hierarchy.id + "/";
				}
				
				var needsQuestionMark = true;
				
				if (searchTerm!=null && searchTerm!=undefined && searchTerm!=""){
					if (needsQuestionMark) { url = url + '?';} else {url = url + '&';}
					url = url + 'search='+searchTerm;				
					needsQuestionMark = false;
				} 				
				
				if (withImages!=null){
					if (needsQuestionMark) { url = url + '?';} else {url = url + '&';}
					url = url + "withImages=" + withImages;
					needsQuestionMark = false;
				}
				
				return $http.get(url);
			},
			modify: function(product){
				return $http({ method: 'PUT', url: '/product/modify/', data: product });
			},
			addImage: function(product,file){
				var upload = Upload.upload({
					url: '/product/'+product.id+'/image/add/',
					data: { images: file }
				});
				return upload;
			},
			removeImage: function(product,image){
				return $http({method: 'POST', url: '/product/'+product.id+'/image/remove/'+image.id});
			},
			uploadFile: function(hierarchy,file){
				var upload = Upload.upload({
		            url: '/product/create/from/file/'+hierarchy.id,
		            data: {file: file}
				});
								
				return upload;
			}
			
		}		
	});
	
	
})();