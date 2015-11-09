(function(){
	
	angular.module('products-services',['ngFileUpload'])
	.factory('Product', function ProductFactory($http,Upload){
		var factory = this;
		
		this.cleanUpProduct = function cleanUpProduct(product){
			if (product.amazonReference == ""){
				product.amazonReference = null;
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
			search: function(hierarchy,searchTerm,withImages,owned){				
				var url = "/product/search/";
												
				if (
						hierarchy!=null &&
						hierarchy!=undefined 	
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
				
				if (owned!=null){
					if (needsQuestionMark) { url = url + '?';} else {url = url + '&';}
					url = url + "owned=" + owned;
					needsQuestionMark = false;
				}
				
				return $http.get(url,{
					progressbar: true
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
			}
			
		}		
	});
	
	
})();