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
		
		return {
			getHeaderRequestParameter: function(){
				return factory.headerWithSessionRequest;
			},			
			getSession: function(){
				return factory.session;
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
							factory.session = headers(factory.headerWithSessionResponse) ;
						})
						.catch(function(){
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
						factory.session = headers(factory.headerWithSessionResponse);
						factory.setStoredSession(factory.session);
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
