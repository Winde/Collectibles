(function(){

	angular.module('projectProgressBar')
	.factory('httpInjectProgress', ['$q','$rootScope','ngProgressFactory',
	       function httpInjectProgress($q, $rootScope,ngProgressFactory) {
		
		var factory = this;
		this.progressBarColor = '#fff';
		   
		  return {
			 'request': function(request) {
				 if (request && request.nointercept){
					 
				 } else {
					 if (request && request && request.progressbar){
						 if ($rootScope.progressbar && $rootScope.progressbar.complete){
							 $rootScope.progressbar.complete();
						 }
		    			$rootScope.progressbar = ngProgressFactory.createInstance();
		    			$rootScope.progressbar.setColor(factory.progressBarColor);
		    			$rootScope.progressbar.start();
					 }
				 }
				 return request;
			 },
	    	'response': function(response){
	    		if (response && response.config && response.config.nointercept){
	    		
	    		} else {
		    		if (response && response.config && response.config.progressbar){
		    			if ($rootScope.progressbar){
		    				$rootScope.progressbar.complete();
		    			}
		    		}
	    		}
	    		return response;
	    	},
			 'responseError': function(responseError){								
	        	if (responseError && responseError.config && responseError.config.nointercept) {	        	
	        	} else {		    	        		
	        		if (responseError && responseError.config && responseError.config.progressbar){
		        		if ($rootScope.progressbar){
		        			$rootScope.progressbar.complete();
		    			}
		    		}	        		            
	        	} 
	        	return $q.reject(responseError);	        	
			 }
		  }
	}])
	
})();