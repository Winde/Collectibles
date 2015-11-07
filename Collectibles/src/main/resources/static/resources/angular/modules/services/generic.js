(function(){
	
	angular.module('generic-services',[])
	.factory('Message', function MessageFactory(){
		var config = {
				  ele: 'body', // which element to append to
				  type: 'info', // (null, 'info', 'danger', 'success')
				  offset: {from: 'bottom', amount: 20}, // 'top', or 'bottom'
				  align: 'right', // ('left', 'right', or 'center')
				  width: 'auto', // (integer, or 'auto')
				  delay: 4000, // Time while the message will be displayed. It's not equivalent to the *demo* timeOut!
				  allow_dismiss: true, // If true then will display a cross to close the popup.
				  stackup_spacing: 10 // spacing between consecutively stacked growls.
		};
		
		return {
			info: function(message,important){
				var newconfig = angular.copy(config,newconfig);
				newconfig.type = "info";	
				if (important){
					newconfig.align = "center";
					newconfig.offset =  {from: 'top', amount: 150};	
				}
				jQuery.bootstrapGrowl(message,newconfig); 
			},
			alert: function(message,important){
				var newconfig = angular.copy(config,newconfig);
				newconfig.type = "danger";
				if (important){
					newconfig.align = "center";
					newconfig.offset =  {from: 'top', amount: 150};	
				}
				jQuery.bootstrapGrowl(message,newconfig); 
			},
			success: function(message,important){
				var newconfig = angular.copy(config,newconfig);
				newconfig.type = "success";		
				if (important){
					newconfig.align = "center";
					newconfig.offset =  {from: 'top', amount: 150};	
				}
				jQuery.bootstrapGrowl(message,newconfig); 
			}
		};		
	});
	
})();