(function(){
	
	angular.module('hierarchies-services',[])
	.factory('Hierarchy', function HierarchyFactory($http){
		
		var maxHierarchyLevels = 3;
		
		function addChildren(array, parent, children, depth, maxDepth){
			for (var j=0;j<children.length;j++){				
				children[j].group = parent.name;
				array.push(children[j]);
				if ((maxDepth== undefined || depth<maxDepth) && children[j].children!=null && children[j].children.length > 0) {				
					children[j].isGroup = true;	
					console.log(JSON.stringify(array, ["name","depth"]));
					addChildren(array, children[j], children[j].children, depth +1, maxDepth);
				} else {
					children[j].isElement = true;
				}
			}			
		}
		
		return {
			root: function(){				
				return $http.get('/hierarchy/root/');				
			},
			calculateTree: function(root,destination){
				if (root.children!=null && root.children!=undefined && root.children.length>0){							
					addChildren(destination, root, root.children, 1);
					console.log(JSON.stringify(destination, ["name","depth"]));
				}
			},
			create: function(hierarchy, parent){
				return $http({url: '/hierarchy/create/'+parent.id+'/', method: 'POST', data: hierarchy});
			}
		}
		
	});
	
})();