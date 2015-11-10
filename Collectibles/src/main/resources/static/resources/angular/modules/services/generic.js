(function(){
	
	angular.module('generic-services',[])
	.factory('Message', function MessageFactory(){
		
		var factory = this;
		
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
		
		this.newConfig = function(important,time){
			var newconfig = angular.copy(config,newconfig);
			if (important){
				newconfig.align = "center";
				newconfig.offset =  {from: 'top', amount: 150};	
			}
			if (time){
				newconfig.delay = time;
			}
			return newconfig;
		};
		
		return {
			info: function(message,important,time){
				var newconfig = factory.newConfig(important,time);
				newconfig.type = "info";					
				jQuery.bootstrapGrowl(message,newconfig); 
			},
			alert: function(message,important,time){
				var newconfig = factory.newConfig(important,time);
				newconfig.type = "danger";							
				jQuery.bootstrapGrowl(message,newconfig); 
			},
			success: function(message,important,time){
				var newconfig = factory.newConfig(important,time);
				newconfig.type = "success";						
				jQuery.bootstrapGrowl(message,newconfig); 
			},
			confirm: function(message,callback){
				if (bootbox){
					bootbox.confirm(message, function(result) {						
						if (result){
							callback();
						}
		            });			
				}else {
					if (confirm(message)){
						callback();
					}
				}
			} 
			
		};		
	});
	
})();