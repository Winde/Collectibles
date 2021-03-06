(function(){
	
	angular.module('hierarchy')
	.factory('Hierarchy', function HierarchyFactory($http){
		
		var maxHierarchyLevels = 3;
		
		function addChildren(array, parent, children, maxDepth, optionalToAddChildren){
			for (var j=0;j<children.length;j++){
				var current = children[j];
				if (parent!=null){
					current.parent = angular.copy(parent);
					current.parent.children = null;
				}
				var depth = current.depth;					
				
				var optionalLineageParent = null;
				if (optionalToAddChildren && optionalToAddChildren.lineage){
					var lastIndexOfSeparator = optionalToAddChildren.lineage.lastIndexOf("-");
					if (lastIndexOfSeparator>0){
						optionalLineageParent = optionalToAddChildren.lineage.substr(0,lastIndexOfSeparator);	
					}					
				}
				
				if (	
						//If we are still below the default depth
						(maxDepth==undefined || depth<=maxDepth) ||
						
						//If we are traversing the path to our desired node
						(current && optionalToAddChildren && current.lineage && optionalToAddChildren.lineage && optionalToAddChildren.lineage.indexOf(current.lineage)==0) ||
						
						//We also add the children of our target node
						(optionalToAddChildren && parent && (parent == optionalToAddChildren)) ||
						
						//We also would prefer to see the siblings
						(optionalToAddChildren && optionalToAddChildren.depth && optionalLineageParent && current && current.lineage && current.lineage.indexOf(optionalLineageParent)==0 && (depth == optionalToAddChildren.depth))
					){
						current.group = parent.name;
						array.push(current);
						if (current.children!=null && current.children.length > 0) {				
							current.isGroup = true;						
							addChildren(array, current, current.children, maxDepth, optionalToAddChildren);
						} else {
							current.isElement = true;
						}
				}
			}			
		}
		
		function findIdInTree(node,id) {
			var selectedNode = null;
			
			if (node.id == id){
				selectedNode = node;
			} else {
				if (node.children && node.children.length>0){
					for (var i=0;i<node.children.length;i++){
						var selection = findIdInTree(node.children[i],id);
						if (selection!=null){
							selectedNode = selection;
							break;
						}
					}
				}				
			}
			return selectedNode;
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
			calculateTreeFromSelection: function(root,destination,idToSearch,defaultMaxDepth){
				if (root.children!=null && root.children!=undefined && root.children.length>0){	
					var nodeFound = findIdInTree(root,idToSearch);

					if (nodeFound){
						addChildren(destination, root, root.children, defaultMaxDepth,nodeFound);
					} else {
						this.calculateTree(root,destination,defaultMaxDepth);
					}	
				}				
				
			},
			calculateConnectorNames: function(node){
				var connectorNames = [];
				var current = node;
				while(current.parent){									
					if (current.connectorNames && current.connectorNames.length>0){		
						connectorNames = connectorNames.concat(current.connectorNames);						
					}
					current = current.parent;
				}
				return connectorNames;
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