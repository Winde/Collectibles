(function(){

	angular.module('collections').config(function($httpProvider,$routeProvider){
		
		$httpProvider.interceptors.push('httpInjectProgress')
		$httpProvider.interceptors.push('httpInjectAuth');

		var routerConfig = this;
		
		
		
		this.secured = function($location,$q,Auth){
			var deferred = $q.defer();			
			if (!Auth.isloggedIn()){							
				deferred.reject();
		        $location.url('/products/');
			} else {
				deferred.resolve();
			}
		}
				
		$routeProvider
		.when('/login/', {
			templateUrl : '/app/templates/login/login.html',
			secured: false
		})
		.when('/products/', {
			templateUrl: '/app/templates/products/search.html',
			secured: false,
			reloadOnSearch: false
		})
		.when('/product/:id', {
			templateUrl: '/app/templates/products/details.html',
			secured: false
		})
		.when('/products/create/', {
			templateUrl: '/app/templates/products/create.html',
			secured: true,
			resolve: { loggedIn: routerConfig.secured }
		})
		.when('/hierarchy/create/', {
			templateUrl: '/app/templates/hierarchies/create.html',
			secured: true,
			resolve: { loggedIn: routerConfig.secured }
		})
		.otherwise({
			redirectTo: '/login/'
		});				
	})
	.factory('httpInjectProgress', ['$q','$rootScope','ngProgressFactory',
	       function httpInjectProgress($q, $rootScope,ngProgressFactory) {
		
		var factory = this;
		this.progressBarColor = '#fff';
		   
		  return {
			 'request': function(request) {
				 if (request && request.nointercept){
					 
				 } else {
					 if (request && request && request.progressbar){			    			
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
	
	.factory('httpInjectAuth', ['$q', 'Auth','$location',
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

