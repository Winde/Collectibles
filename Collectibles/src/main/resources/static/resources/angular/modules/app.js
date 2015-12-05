(function(){
	
	angular.module('login',[]);
	angular.module('product',[])	
	angular.module('hierarchy',[]);
	angular.module('image',[]);
	angular.module('user',[]);
	angular.module('generic-functionalities',[]);
	angular.module('projectProgressBar',['ngProgress']);
	
	var app = angular.module('collections', 
	['ngRoute',
	 'ngSanitize',
	 'ngAnimate',
	 'projectProgressBar',
	 'ngFileUpload',
	 'afkl.lazyImage',
	 'monospaced.elastic',
	 'isoCurrency',
	 //'ngCookies',	 
	 'ui.bootstrap',
	 'login',
	 'product',	 	 
	 'hierarchy',
	 'image', 	 
	 'user',
	 'generic-functionalities']);
	
	

	

})();

