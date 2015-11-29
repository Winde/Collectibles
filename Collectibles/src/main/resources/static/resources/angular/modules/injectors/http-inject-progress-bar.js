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
						if (!$rootScope.progressBarInstance){ 
							$rootScope.progressBarInstance = ngProgressFactory.createInstance();
							$rootScope.progressBarInstance.setColor(factory.progressBarColor);
						}							
						$rootScope.progressBarInstance.start();
					 }
				 }
				 return request;
			 },
	    	'response': function(response){
	    		
	    		if (response && response.config && response.config.progressbar){
	    			if ($rootScope.progressBarInstance){		    				
	    				$rootScope.progressBarInstance.complete();		    				
	    			}
	    		}
	    		
	    		return response;
	    	},
			 'responseError': function(responseError){								
	        			    	        		
        		if (responseError && responseError.config && responseError.config.progressbar){
        			if ($rootScope.progressBarInstance){		    				
	    				$rootScope.progressBarInstance.complete();		    				
	    			}
	    		}	        		            
	        	
	        	return $q.reject(responseError);	        	
			 }
		  }
	}])
	
})();