(function(){

	angular.module('login')	
	.factory('Base64',function(){
		return {
			encode: function(string){
				return btoa(string);
			}
		}
	} )
	
})();
