(function(){
	
	angular.module('generic-services',[])
	.factory('Message', function MessageFactory(){
		var config = {
				  ele: 'body', // which element to append to
				  type: 'info', // (null, 'info', 'danger', 'success')
				  offset: {from: 'top', amount: 20}, // 'top', or 'bottom'
				  align: 'right', // ('left', 'right', or 'center')
				  width: 250, // (integer, or 'auto')
				  delay: 4000, // Time while the message will be displayed. It's not equivalent to the *demo* timeOut!
				  allow_dismiss: true, // If true then will display a cross to close the popup.
				  stackup_spacing: 10 // spacing between consecutively stacked growls.
		};
		
		return {
			info: function(message){				
				config.type = "info";				
				jQuery.bootstrapGrowl(message); 
			},
			alert: function(message){
				config.type = "danger";				
				jQuery.bootstrapGrowl(message,config); 
			},
			success: function(message){
				config.type = 'success';				
				jQuery.bootstrapGrowl(message,config); 
			}
		};		
	});
	
})();