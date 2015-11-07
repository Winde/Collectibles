(function(){

	angular.module('collections').config(function($httpProvider,$routeProvider){
		
		$httpProvider.interceptors.push('responseObserver');

		$routeProvider
		.when('/login/', {
			templateUrl : '/app/templates/login/login.html',			
		})
		.when('/products/', {
			templateUrl: '/app/templates/products/search.html'
		})		
		.when('/products/create/', {
			templateUrl: '/app/templates/products/create.html',									
		})
		.when('/product/:id', {
			templateUrl: '/app/templates/products/details.html',
		})
		.when('/hierarchy/create/', {
			templateUrl: '/app/templates/hierarchies/create.html',
		})
		.otherwise({
			redirectTo: '/login/'	
		});				
	})
	.factory('responseObserver', ['$q', 'Auth','$location',
	     function responseObserver($q, Auth,$location) {
		    return {
		    	
		    	'request': function(request){
		    				    		
		    		if (request && request.config && request.config.nointercept){
		    			return request;
		    		}else{
		    		
			    		if (Auth.isloggedIn()){
			    			var auth = Auth.getAuthData();					    			
				    		request.headers['Authorization'] = 'Basic '+ auth;			    			
			    		}			 
			    		
			    		request.headers['X-Requested-With'] = 'XMLHttpRequest';
			    		return request;
		    		}
		    	},		    	
		        'responseError': function(errorResponse) {

		        	if (errorResponse && errorResponse.config && errorResponse.config.nointercept) {
		        		return $q.reject(errorResponse);
		        	} else {		        		
	                    // $http is already constructed at the time and you may
	                    // use it, just as any other service registered in your
	                    // app module and modules on which app depends on.	        			
	        			switch (errorResponse.status) {
				            case 403:			            	
				            	Auth.logout();			            				            
				            	$location.path('/login/').replace();			            	
				            break;	            
			            }
			            return $q.reject(errorResponse);		        				        					            
		        	}
		        }
		    };
	}]);
	
})();

