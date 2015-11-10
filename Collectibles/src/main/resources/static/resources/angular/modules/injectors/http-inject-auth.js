(function(){
	

	angular.module('login')
	.factory('httpInjectAuth',['$q', 'Auth','$location',
			 function httpInjectAuth($q, Auth,$location) {
			
		    return {		    	
		    	'request': function(request){		    		
		    		if (request && request.nointercept){		    			
		    		
		    		}else{		    			    			
		    			if (Auth.isloggedIn()){
			    			var session = Auth.getSession();
			    			if (session!=null){
			    				request.headers[Auth.getHeaderRequestParameter()] = session;
			    			}
			    		}			 			    		
			    		request.headers['X-Requested-With'] = 'XMLHttpRequest';		    	
		    		}		    		
		    		return request;
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
				            	$location.path('/login/').replace();						            	
				            break;	            
			            }	        				        			
			            //return $q.reject(responseError);		        				        					            
		        	}
		        	return $q.reject(responseError);
		        }
		    };
	}]);

})();