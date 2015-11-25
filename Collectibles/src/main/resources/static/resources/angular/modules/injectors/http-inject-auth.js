(function(){
	

	angular.module('login')
	.factory('httpInjectAuth',['$q', 'Message','Auth','Base64','$location',
			 function httpInjectAuth($q, Message, Auth, Base64, $location) {
			
		    return {		    	
		    	'request': function(request){		    		
		    		if (request && request.nointercept){		    			
		    		
		    		}else{		    			    			
		    			if (Auth.isloggedIn()){
			    			var session = Auth.getSession();
			    			if (session!=null){
			    				request.headers[Auth.getHeaderRequestParameter()] = session;
			    				console.log("Injecting token expires: "+ JSON.parse(atob(session.split(".")[0])).expires);
			    			}
			    		}			 			    		
			    		request.headers['X-Requested-With'] = 'XMLHttpRequest';		    	
		    		}		    		
		    		return request;
		    	},	
		    	'response': function(response){		    	
		    		if (response && response.config && response.config.nointercept) {	
			    		
		    		} else {
		    			if (response.headers && response.headers(Auth.getHeaderResponseParameter())){
		    				console.log(response);
		    				var newSession = response.headers(Auth.getHeaderResponseParameter());		    				
		    				var currentSession = Auth.getSession();
		    				if (currentSession==null || currentSession == undefined){
		    					Auth.setStoredSession(newSession);	
		    				} else if (newSession!=null || newSession == undefined){
		    					
		    				} else if (Auth.getExpires(newSession) && Auth.getExpires(newSession)>Auth.getExpires(currentSession)){
		    					Auth.setStoredSession(newSession);
		    				} 		    			
			    		}
	    				
	    				if (response.headers('X-AUTH-EXPIRE')){
	    					
	    					if (Auth.isloggedIn()){
	    						Message.info("Your session has expired");
	    						Auth.logout();
	    					}		    										
	    				}
		    		}
		    		
		    		return response;
		    	},
		        'responseError': function(responseError) {		        	
		        	if (responseError && responseError.config && responseError.config.nointercept) {		        		
		        		//return $q.reject(responseError);
		        	} else {		        				
	                    // $http is already constructed at the time and you may
	                    // use it, just as any other service registered in your
	                    // app module and modules on which app depends on.		        		
	        			switch (responseError.status) {
				            case 403:				            	
				            	Auth.logout();						            	
				            	if ($location.path()!='/products/'){
				            		$location.path('/products/').replace();	
				            	}
				            				            	
				            	Message.alert("Your session has expired",true);
				            break;	            
			            }	        				        			
			            //return $q.reject(responseError);		        				        					            
		        	}
		        	return $q.reject(responseError);
		        }
		    };
	}]);

})();