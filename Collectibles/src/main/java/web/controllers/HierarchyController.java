package web.controllers;

import model.dataobjects.HierarchyNode;
import model.dataobjects.HierarchyNode.HierarchyTreeView;
import model.persistence.HierarchyRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import web.supporting.error.exceptions.CollectiblesException;
import web.supporting.error.exceptions.GenericException;
import web.supporting.error.exceptions.IncorrectParameterException;
import web.supporting.error.exceptions.NotFoundException;

@RestController
public class HierarchyController extends CollectiblesController{
		
	@Autowired
	private HierarchyRepository hierarchyRepository;
	
	@JsonView(HierarchyTreeView.class)
	@RequestMapping(value="/hierarchy/root/", method = RequestMethod.GET)
	public HierarchyNode hierarchyRoot() throws CollectiblesException{
		HierarchyNode hierarchyNode = hierarchyRepository.findRoot();
		if (hierarchyNode==null) {
			throw new NotFoundException();
		} else {
			return hierarchyNode;
		}
	}
	
	@RequestMapping(value="/hierarchy/get/{id}", method = RequestMethod.GET)
	public HierarchyNode subHierarchy(@PathVariable String id) throws CollectiblesException{
		Long nodeId = this.getId(id);
		
		if (nodeId!=null){
			HierarchyNode hierarchyNode = hierarchyRepository.findOne(nodeId);
			if (hierarchyNode==null) {
				throw new NotFoundException();
			} else {
				return hierarchyNode;
			}			
		}else {
			throw new IncorrectParameterException(new String[]{"id"});
		}
	}
	
	
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/hierarchy/remove/{id}", method = RequestMethod.DELETE)
	public boolean removeHierarchyNode(@PathVariable String id) throws CollectiblesException {
		Long hierarchyId = this.getId(id);
		
		if (hierarchyId!=null){
			try {
				hierarchyRepository.deleteCascade(hierarchyId);
			}catch(EmptyResultDataAccessException ex) {				
				throw new NotFoundException(new String[]{"hierarchy"});
			}
			return true;
		}else {
			throw new IncorrectParameterException(new String[]{"id"});
		}
	}
	
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/hierarchy/create/{father}/", method = RequestMethod.POST)
	public HierarchyNode addHierarchyNode(@RequestBody HierarchyNode node,@PathVariable String father) throws CollectiblesException {
		Long fatherId = this.getId(father);
			
		if (fatherId!=null){
			this.validate(node);
			
			if (node.getId()!=null){
				throw new IncorrectParameterException(new String[]{"category.id"});
			}
			
			HierarchyNode fatherNode = hierarchyRepository.findOne(fatherId);
			
			if (fatherNode!=null){			
				if (node.getIsBook()==null){
					node.setIsBook(fatherNode.getIsBook());
				}
				hierarchyRepository.addChild(fatherNode, node);								
				return node;
			} else{				
				throw new NotFoundException(new String[]{"father"});
			}
			
		} else {			
			throw new IncorrectParameterException(new String[]{"father"});
		}
		
	}
}
