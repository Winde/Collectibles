(function(){

	angular.module('generic-functionalities')
	.directive('fluidbox', function($timeout){
		return {
			restrict: 'A',
			compile: function compile(tElement, tAttrs, transclude) {
				return {
					pre: function preLink(scope, iElement, attrs, controller) { },
					post: function postLink(scope, iElement, attrs, controller) {						
						attrs.$observe('src', function(newValue) {
							$(iElement).parent().fluidbox();
						});
					}
				}
			}				
		};		
	});

})();