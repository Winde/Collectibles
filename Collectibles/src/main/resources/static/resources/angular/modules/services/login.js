(function(){

	angular.module('login-services',[])	
	.factory('Auth', function HierarchyFactory(Base64,Message,$injector,$location){
		this.authenticated = false;
		this.credentials = null;		
		var factory = this;
		
		return {
			getAuthData: function(){
				var authdata = "";
				if (factory.credentials && factory.credentials.username && factory.credentials.password){
					authdata = Base64.encode(factory.credentials.username + ':' + factory.credentials.password);
				}
				return authdata;
			},					
			login: function(credentials,callbackError){		
				
				$injector.invoke(function($http) {					
					var authdata = null;
					if (credentials.username && credentials.password){
						authdata = Base64.encode(credentials.username + ':' + credentials.password);
					}
					
					$http({
						url: '/login', 
						method: 'POST', 
						nointercept: true, 
						headers: {
							'Authorization' : 'Basic ' + authdata,
							'X-Requested-With': 'XMLHttpRequest'						
						}						
					})
					.success(function(data){						
						factory.credentials = credentials;
						factory.authenticated = true;						
						$location.path("/products/").replace();
					})
					.catch(function(){
						callbackError();
						Message.alert("Username / Password is not correct");
					})
					.finally(function(){
						
					});
				});
				
			},
			logout: function(){
				factory.authenticated = false;
				factory.credentials = {};
			},
			isloggedIn: function(){
				return factory.authenticated;
			}
		};
		
		
	})
	.factory('Base64',function(){
		return {
			encode: function(string){
				return btoa(string);
			}
		}
	} )
	
})();
