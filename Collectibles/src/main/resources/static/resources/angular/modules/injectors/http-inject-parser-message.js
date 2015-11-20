(function(){

	angular.module('product')
	.factory('httpInjectParserMessage', ['Message',function httpInjectParserMessage(Message) {
		
		var factory = this;
		
		  return {			
	    	'response': function(response){
	    		if (response.status == 206){
	    			Message.info("Server is busy, scrape request has been queued");    			
	    		}
	    	
	    		return response;
	    	}
		  }
	}])
	
})();
