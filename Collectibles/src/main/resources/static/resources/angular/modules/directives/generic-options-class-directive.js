(function(){
	
	angular.module('generic-functionalities')
	.directive('optionsClass', function ($parse) {
	    return {
	        require: 'select',
	        link: function (scope, elem, attrs, ngSelect) {
	            // get the source for the items array that populates the select.
	        	
	        	var sourceOptions = attrs.ngOptions;
	        		        	
	        	if (sourceOptions && sourceOptions.indexOf(" track by ")>0){
	        		sourceOptions  = sourceOptions.substr(0,sourceOptions.indexOf(" track by "));
	        	} 	        	
	        	var optionsSourceStr = sourceOptions.split(' ').pop();
	        	
	            // use $parse to get a function from the options-class attribute
	            // that you can use to evaluate later.
	            var getOptionsClass = $parse(attrs.optionsClass);
	            
	            scope.$watchCollection(optionsSourceStr, function (items) {
	                scope.$$postDigest(function () {
	                    // when the options source changes loop through its items.	                	
	    	            
	                    angular.forEach(items, function (item, index) {
	                        // evaluate against the item to get a mapping object for
	                        // for your classes.
	                    	
	                    	
	                        var classes = getOptionsClass(item);
	                        // also get the option you're going to need. This can be found
	                        // by looking for the option with the appropriate index in the
	                        // value attribute.
	                        
	                        var thereIsEmptyOption = $(elem).find('option:not([label])').length!=0;
	                        
	                        if (thereIsEmptyOption){
	                        	index = index +1;
	                        } 
	                        	                        
	                        var option = elem.find('option')[index];
	                        
	                        var classesString = "";
	                        
	                        // now loop through the key/value pairs in the mapping object
	                        // and apply the classes that evaluated to be truthy.
	                        angular.forEach(classes, function (add, className) {
	                            if (add) {
	                            	classesString= classesString + " " + className;	                                
	                            }
	                        });
	                        $(angular.element(option)).removeClass();	                        
	                        angular.element(option).addClass(classesString);
	                    });	                    
	                    //if ($(elem).is('.selectpicker')){	                    	
	                    //	$(elem).selectpicker('refresh');
	                    //}
	                });
	            });
	        }
	    };
	});
	
	
})();