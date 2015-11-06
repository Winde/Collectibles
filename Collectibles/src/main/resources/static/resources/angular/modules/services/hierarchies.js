(function(){
	
	angular.module('hierarchies-services',[])
	.factory('Hierarchy', function HierarchyFactory($http){
		return {
			root: function(){				
				return $http.get('/hierarchy/root/');				
			},
			calculateTree: function(root,destination){
				if (root.children!=null && root.children!=undefined && root.children.length>0){	
					for (var i=0;i<root.children.length;i++){
						destination.push(root.children[i]);
						root.children[i].father = null;
						root.children[i].group = root.children[i].name;
						if (root.children[i].children && root.children[i].children.length>0) {
							for (var j=0;j<root.children[i].children.length;j++){
								root.children[i].children[j].group = root.children[i].name;
								destination.push(root.children[i].children[j]);
							}
						}
					}
				}
			}
		}
		
	});
	
})();