(function(){
	
	angular.module('images-services',[])
	.factory('Image', function ImageFactory($http){
		return {
			one: function(id){
				
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
				return $http({method: 'GET', url: '/images/'+parameter});
			}			
		}
		
	});
	
})();