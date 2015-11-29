(function(){

	angular.module('projectProgressBar')
	.factory('httpInjectProgress', ['$q','$rootScope','ngProgressFactory',
	       function httpInjectProgress($q, $rootScope,ngProgressFactory) {
		
		var factory = this;
		this.progressBarColor = '#F62217';
		   
		  return {
			 'request': function(request) {
				 if (request && request.nointercept){
					 
				 } else {
					 if (request && request && request.progressbar){						 
						if (!request.progressBarInstance){ 
							request.progressBarInstance = ngProgressFactory.createInstance();
							request.progressBarInstance.setColor(factory.progressBarColor);
						}												
						request.progressBarInstance.start();
					 }
				 }
				 return request;
			 },
	    	'response': function(response){
	    		
	    		if (response && response.config && response.config.progressbar){
	    			if (response.config.progressBarInstance){		    				
	    				response.config.progressBarInstance.complete();		    				
	    			}
	    		}
	    		
	    		return response;
	    	},
			 'responseError': function(responseError){								
	        			    	        		
        		if (responseError && responseError.config && responseError.config.progressbar){
        			if (responseError.config.progressBarInstance){		    				
        				responseError.config.progressBarInstance.complete();		    				
	    			}
	    		}	        		            
	        	
	        	return $q.reject(responseError);	        	
			 }
		  }
	}])
	
})();