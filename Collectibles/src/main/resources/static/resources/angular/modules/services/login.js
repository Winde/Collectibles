(function(){

	angular.module('login-services',[])	
	.factory('Auth', function HierarchyFactory(Base64,Message,$injector,$location,$cookies){
		this.authenticated = false;
		this.session = null;		
		var factory = this;
		
		this.headerWithSessionRequest = "X-AUTH-TOKEN";
		this.headerWithSessionResponse = "X-AUTH-TOKEN";
		var localStorageKeyForSession = "X-AUTH-TOKEN";
		
		
		
		this.getStoredSession = function() {
			return localStorage.getItem(localStorageKeyForSession);			
		}
		
		this.setStoredSession = function(session){
			localStorage.setItem(localStorageKeyForSession,session);
		}
		
		this.removeStoredSession = function(){			
			localStorage.removeItem(localStorageKeyForSession);
			/*
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
		    */		    
		}
		
		if (this.getStoredSession()!=null){
			this.authenticated = true;
		}
		
		return {
			getHeaderRequestParameter: function(){
				return factory.headerWithSessionRequest;
			},			
			getSession: function(){
				return factory.getStoredSession();
			},						
			checkSessionIsSet: function(){
				var storedSession = factory.getStoredSession();				
				if (factory.getStoredSession()!=null){
					$injector.invoke(function($http){
						
						headers = {};
						headers[factory.headerWithSessionRequest] = factory.getStoredSession();
						$http({
							url: '/checksession', 
							method: 'POST', 
							nointercept: true, 
							headers: headers				
						})
						.success(function(data ,status,headers){		
							factory.authenticated = true;							
						})
						.catch(function(){
							factory.authenticated = false;
							factory.removeStoredSession();
						})
					});
				} else {
					console.log("Skipping because no session");
				}
								
			},			
			login: function(credentials,callbackError){						
				$injector.invoke(function($http) {															
					$http({
						url: '/login', 
						method: 'POST', 
						nointercept: true, 
						data: $.param(credentials),
						headers: {'Content-Type': 'application/x-www-form-urlencoded'}				
					})
					.success(function(data ,status,headers){												
						factory.setStoredSession(headers(factory.headerWithSessionResponse));
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
