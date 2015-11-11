(function(){
	
	angular.module('login',[]);
	angular.module('product',[])	
	angular.module('hierarchy',[]);
	angular.module('image',[]);
	angular.module('generic-functionalities',[]);
	angular.module('projectProgressBar',['ngProgress']);
	
	var app = angular.module('collections', 
	['ngRoute',
	 'ngSanitize',
	 'ngAnimate',
	 'projectProgressBar',
	 'ngFileUpload',
	 'afkl.lazyImage',
	 //'ngCookies',	 
	 'ui.bootstrap',
	 'login',
	 'product',	 	 
	 'hierarchy',
	 'image', 	 
	 'generic-functionalities']);
	
	

	

})();

