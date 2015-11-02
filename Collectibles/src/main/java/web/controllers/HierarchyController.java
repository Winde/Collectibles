package web.controllers;

import model.dataobjects.HierarchyNode;
import model.persistence.HierarchyRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import web.supporting.error.exceptions.CollectiblesException;
import web.supporting.error.exceptions.GenericException;
import web.supporting.error.exceptions.IncorrectParameterException;
import web.supporting.error.exceptions.NotFoundException;

@RestController
public class HierarchyController extends CollectiblesController{
		
	@Autowired
	private HierarchyRepository hierarchyRepository;
	
	@RequestMapping("/hierarchy/root/")
	public HierarchyNode hierarchyRoot() throws CollectiblesException{
		HierarchyNode hierarchyNode = hierarchyRepository.findRoot();
		if (hierarchyNode==null) {
			throw new NotFoundException();
		} else {
			return hierarchyNode;
		}
	}
	
	@RequestMapping("/hierarchy/get/{id}")
	public HierarchyNode subHierarchy(@PathVariable String id) throws CollectiblesException{
		Long nodeId = null;
		try {
			nodeId = Long.parseLong(id);
		}catch(Exception ex){}
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

	@RequestMapping(value="/hierarchy/remove/{id}", method = RequestMethod.POST)
	public boolean removeHierarchyNode(@PathVariable String id) throws CollectiblesException {
		Long hierarchyId = null;
		try {
			hierarchyId = Long.parseLong(id);
		}catch(Exception ex){}
		if (hierarchyId!=null){
			try {
				hierarchyRepository.delete(hierarchyId);
			}catch(EmptyResultDataAccessException ex) {				
				throw new NotFoundException(new String[]{"hierarchy"});
			}
			return true;
		}else {
			throw new IncorrectParameterException(new String[]{"id"});
		}
	}
	
	@RequestMapping(value="/hierarchy/create/{father}/", method = RequestMethod.POST)
	public HierarchyNode addHierarchyNode(@RequestBody HierarchyNode node,@PathVariable String father) throws CollectiblesException {
		Long fatherId = null;
		try {
			fatherId = Long.parseLong(father);
		}catch(Exception ex){}
			
		if (fatherId!=null){
			this.validate(node);
						
			HierarchyNode fatherNode = hierarchyRepository.findOne(fatherId);
			
			if (fatherNode!=null){				
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
