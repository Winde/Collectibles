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
				console.log("Checking Stored Session");
				var storedSession = factory.getStoredSession();				
				//if (factory.getStoredSession()!=null){
					$injector.invoke(function($http){
						$http({
							url: '/login', 
							method: 'POST', 
							nointercept: true, 
							headers: {
								'JSESSIONID' : factory.getStoredSession(),
								'X-Requested-With': 'XMLHttpRequest'
							}						
						})
						.success(function(){		
							console.log("Checking Stored Session Success");
							factory.authenticated = true;
							factory.session = storedSession;
						})
						.catch(function(){
							console.log("Checking Stored Session Failure");
						})
					});
				//} else {
				//	console.log("Skipping because no session");
				//}
								
			},			
			login: function(credentials,callbackError){						
				$injector.invoke(function($http) {										
					var authdata = null;
					if (credentials && credentials.username && credentials.password){
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
