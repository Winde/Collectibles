(function(){
	
	var app = angular.module('generic-directives',[]);

	app.directive('ajax', function(){
		return {
			restrict: 'E',
			templateUrl: 'snipet/ajax.html'				
		};		
	});
	
})();