(function(){
	
	angular.module('generic-functionalities')
	.directive("selectpicker",
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

                                		if (scope[element.attr("data-collection-name")].length>$(element).find('option').length){
                                			index = index +1;
                                		}

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