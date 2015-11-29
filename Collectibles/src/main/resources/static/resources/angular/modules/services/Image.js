(function(){
	
	angular.module('image')
	.factory('Image', function ImageFactory(Domain,$http){
		return {
			one: function(image){
				return $http({method: 'GET', url: Domain.base()+'/images/'+image.id});
			},
			multiple: function(images){
				var parameter = "";
				var i=0;
				for (var i=0;i<images.length; i++){
					var image = images[i];					
					parameter = parameter + image.id					
					if (i<images.length-1){
						parameter = parameter + ",";
					}
				}				
				return $http({method: 'GET', url: Domain.base()+'/images/'+parameter});
			},
			modifyLite: function(image){
				return $http({ 					 
					url: Domain.base()+'/image/modify/minor/',
					method: 'PUT',
					data: image,
					progressbar: true
				});
			},
		}
		
	});
	
})();