(function(){

	angular.module('login')	
	.factory('Auth', function HierarchyFactory(Base64,Message,$injector,$location){
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
		}
		
		
		this.setRoles = function(roles){
			localStorage.setItem("roles",roles);
		}
		
		this.getRoles = function(){
			var roles = localStorage.getItem("roles");
			if (roles!=null && roles!=undefined){
				return JSON.parse(roles);
			}
			return null;
		}
		
		this.removeRoles = function(){
			localStorage.removeItem("roles");
		}
		
		if (this.getStoredSession()!=null){
			this.authenticated = true;
		}
		
		return {
			getRoles: function(){
				return factory.getRoles();
			},
			setRoles: function(roles){
				factory.setRoles(roles);
			},
			removeRoles: function(roles){
				factory.removeRoles(roles);
			},
			setStoredSession : function(session){
				factory.setStoredSession(session);
			},
			getHeaderResponseParameter: function(){
				return factory.headerWithSessionResponse;
			},
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
			login: function(credentials,callbackSuccess,callbackError){						
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
						if (data!=null && data!=undefined){
							factory.setRoles(JSON.stringify(data));
						}
						factory.authenticated = true;
						callbackSuccess();
						//$location.path("/products/").replace();						
					})
					.catch(function(error){
						callbackError();
						credentials.password = null;
						if (error && error.status == 401){
							Message.alert("Username / Password is not correct",true,3000);
						} else if (error && error.status == 404){
							Message.info("Service is unavailable at this time",true,3000);
						} else {
							Message.info("An error has occurred, please try again",true,3000);
						}						
					})
					.finally(function(){
						
					});
				});
				
			},
			logout: function(){				
				factory.authenticated = false;
				factory.removeStoredSession();
				factory.removeRoles();
			},
			isloggedIn: function(){
				return factory.authenticated;
			},
			isAdmin: function(){
				if (factory.authenticated){
					var roles = factory.getRoles();
					if (roles!=null && roles!=undefined){
						return (roles.indexOf("ROLE_ADMIN")>=0)
					}					
				}
				return false;
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
