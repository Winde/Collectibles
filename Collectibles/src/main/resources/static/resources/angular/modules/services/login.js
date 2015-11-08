(function(){

	angular.module('login-services',[])	
	.factory('Auth', function HierarchyFactory(Base64,Message,$injector,$location,$cookies){
		this.authenticated = false;
		this.session = null;		
		var factory = this;
		
		this.getStoredSession = function(){
			return $cookies.get('JSESSIONID');
		}
		
		this.removeStoredSession = function(){			
		    $cookies.remove('JSESSIONID');
		    $injector.invoke(function($http){
			    $http({
					url: '/logout', 
					method: 'POST', 
					nointercept: true, 
					headers: {
						'JSESSIONID' : factory.getStoredSession(),
						'X-Requested-With': 'XMLHttpRequest'
					}						
				});
		    });
		    
		}
		
		return {
			getSession: function(){
				return factory.session;
			},						
			checkSessionIsSet: function(){
				var storedSession = factory.getStoredSession();;
				console.log("Checking Stored Session: " +  storedSession);
				$injector.invoke(function($http){
					$http({
						url: '/login', 
						method: 'POST', 
						nointercept: true, 
						headers: {
							'JSESSIONID' : factory.getStoredSession(),
							'X-Requested-With': 'XMLHttpRequest'
						}						
					}).success(function(){
						factory.authenticated = true;
						factory.session = storedSession;
					});
				});
				
			},			
			login: function(credentials,callbackError){		
				console.log("DO LOGIN");
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
						factory.session = factory.getStoredSession();
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
				console.log("DO LOGOUT");
				factory.authenticated = false;
				factory.session = null;
				factory.removeStoredSession();
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
