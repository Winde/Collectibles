(function(){
	
	var app = angular.module('generic-directives',[]);

	app.directive('ajax', function(){
		return {
			restrict: 'E',
			templateUrl: '/app/snipet/ajax.html'				
		};		
	});
	
	app.directive('optionsClass', function ($parse) {
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
	                        
	                        var option =option = elem.find('option')[index];
	                        // now loop through the key/value pairs in the mapping object
	                        // and apply the classes that evaluated to be truthy.
	                        angular.forEach(classes, function (add, className) {
	                            if (add) {
	                                angular.element(option).addClass(className);
	                            }
	                        });
	                    });	                    
	                    //if ($(elem).is('.selectpicker')){	                    	
	                    //	$(elem).selectpicker('refresh');
	                    //}
	                });
	            });
	        }
	    };
	});
	
    app.directive("selectpicker",
            [
                "$timeout",'$parse',
                function($timeout,$parse) {
                    return {
                        restrict: "A",
                        require: ["ngModel", "?collectionName",'?eval'],
                        compile: function(tElement, tAttrs, transclude) {
                        	
                            if (angular.isUndefined(tAttrs.ngModel)) {
                                throw new Error("Please add ng-model attribute!");
                            } else if (angular.isUndefined(tAttrs.collectionName)) {
                                throw new Error("Please add data-collection-name attribute!");
                            }

                            
                            return function(scope, element, attrs, ngModel) {
                                if (angular.isUndefined(ngModel)){
                                    return;
                                }
                                
                                $(element).selectpicker();
                                
                                $(element).on('change', function() {
                                	
                                	
                                	
                                	var thereIsEmptyOption = $(element).find('option:not([label])').length!=0;
                                                                    	
                                	var index = parseInt($(element).parent().find('.bootstrap-select li.selected').attr('data-original-index'));
                                	if (thereIsEmptyOption){
                                		index = index-1;
                                	}
                                	
                                	
                                	if (element.attr("data-collection-name")){
	                                	var value = scope[element.attr("data-collection-name")][index];	                                		                                	
	                                	if (element.attr('data-collection-attribute')){
	                                		value = value[element.attr('data-collection-attribute')];
	                                	}
                                	} else {
                                		var value = $(element).val();
                                	}
                                	scope.$apply(function () {
	                                	var parsed = $parse(attrs.ngModel);	
	                            		parsed.assign(scope, value);
	                            		
	                            		if (attrs.ngChange){
                                			scope.$eval(attrs.ngChange)                                			
                                		}   
                                	});
                                	
                                });
                                
                                /*
                                $(element).on("change", function(){                                	
                                	var originalValue = $(this).val();
                                	var value = originalValue;
                                	if (value.indexOf("number:")>=0 && parseInt(value.replace("number:",""))) {
                                		value = parseInt(value.replace("number:",""));
                                	}                                 	
                                	
                                	scope.$apply(function () {
                                		//var parsed = $parse(attrs.ngModel);

                                		//parsed.assign(scope, value);
                                		//element.controller('ngModel').$setViewValue(originalValue);
                                		
                                		if (attrs.ngChange){
                                			scope.$eval(attrs.ngChange)                                			
                                		}                                		
                                	});
                                	
                                });
                                */
                                
                                scope.$watch(attrs.ngModel, function(newVal, oldVal) {
                                                  
                                    if (newVal !== oldVal) {                                    	
                                        $timeout(function() {                                            
                                            //$(element).selectpicker("val", element.val());
                                            $(element).selectpicker("refresh");
                                        });
                                        
                                    }
                                });
                                
                                
                                scope.$watchCollection(attrs.collectionName, function(newVal, oldVal) {                                	
                                    $timeout(function() {                                          	                                        
                                        $(element).selectpicker("refresh");
                                    });
                                });

                                ngModel.$render = function() {
                                   $(element).selectpicker("val", ngModel.$viewValue || "");
                                };

                                ngModel.$viewValue = element.val();
                            };
                        }
                        
                    }
                }
            ]
        );
	
})();