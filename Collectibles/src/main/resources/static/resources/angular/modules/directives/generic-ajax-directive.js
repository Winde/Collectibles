(function(){
	
	angular.module('generic-functionalities')
	.directive('ajax', function(){
		return {
			restrict: 'E',
			templateUrl: '/app/snipet/ajax.html'				
		};		
	});
	
})();