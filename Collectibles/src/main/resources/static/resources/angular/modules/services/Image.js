(function(){
	
	angular.module('image')
	.factory('Image', function ImageFactory(Properties,$http){
		return {
			one: function(image){
				return $http({method: 'GET', url: Properties.baseDomain+'/images/'+image.id});
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
				return $http({method: 'GET', url: Properties.baseDomain+'/images/'+parameter});
			},
			modifyLite: function(image){
				return $http({ 					 
					url: Properties.baseDomain+'/image/modify/minor/',
					method: 'PUT',
					data: image,
					progressbar: true
				});
			},
		}
		
	});
	
})();