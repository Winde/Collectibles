package model.dataobjects.serializable.services;

import model.connection.ProductInfoConnectorFactory;
import model.dataobjects.HierarchyNode;
import model.dataobjects.serializable.SerializableHierarchyNode;
import model.persistence.HierarchyRepository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class SerializableHierarchyNodeService {

	
	@Cacheable(value="root", key="'serializedRoot'", cacheManager="cacheManager")
	public SerializableHierarchyNode getRoot(HierarchyRepository hierarchyRepository, ProductInfoConnectorFactory connectorFactory) {
		HierarchyNode hierarchyNode = hierarchyRepository.findRoot();
		if (hierarchyNode==null) {
			return null;
		} else {
			return new SerializableHierarchyNode(hierarchyNode,connectorFactory);
		}
	}
}
