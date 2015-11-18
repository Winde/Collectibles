(function(){
	
	angular.module('hierarchy')
	.factory('Hierarchy', function HierarchyFactory($http){
		
		var maxHierarchyLevels = 3;
		
		function addChildren(array, parent, children, maxDepth){
			for (var j=0;j<children.length;j++){			
				var depth = children[j].depth;				
				if (maxDepth==undefined || depth<=maxDepth){
					children[j].group = parent.name;
					array.push(children[j]);
					if (children[j].children!=null && children[j].children.length > 0) {				
						children[j].isGroup = true;						
						addChildren(array, children[j], children[j].children, maxDepth);
					} else {
						children[j].isElement = true;
					}
				}
			}			
		}
		
		return {
			root: function(){				
				return $http.get('/hierarchy/root/');				
			},
			calculateTree: function(root,destination,maxDepth){
				if (root.children!=null && root.children!=undefined && root.children.length>0){							
					addChildren(destination, root, root.children, maxDepth);					
				}
			},
			create: function(hierarchy, parent){
				return $http({
					url: '/hierarchy/create/'+parent.id+'/', 
					method: 'POST', 
					data: hierarchy,
					progressbar: true
				});
			},
			remove: function(hierarchy){				
				return $http({
					url: '/hierarchy/remove/'+hierarchy.id+'/', 
					method: 'DELETE',
					progressbar: true
				});
			}
		}
		
	});
	
})();