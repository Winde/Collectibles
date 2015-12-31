(function(){

	angular.module('generic-functionalities').directive('a', function(
			Properties,
			$window
	) { 'use strict';
	return {
		priority: 1,
		restrict: 'E',
		link: function($scope, $element, $attrs) {
			if(!Properties.useHtml5Mode) {
				$attrs.$observe('href', function(value) {
					if(value && value.indexOf('/') === 0) {
						$attrs.$set('href', '#' + value);
					}
				});
				
				/*
				// If using cordova-plugin-inappbrowser make external links open outside the app
				if($attrs.target && $attrs.target === '_blank') {
					angular.element($element).click(function(event) {
						event.preventDefault();
						$window.open($attrs.href, '_system');
					});
				}
				*/
			} else {
				
				$attrs.$observe('href', function(value) {
					if(value && value.indexOf('/') === 0 && !value.startsWith(Properties.base)) {
						$attrs.$set('href', Properties.base + value);
					}
				});
				
			}
		}
	};
	});


})();