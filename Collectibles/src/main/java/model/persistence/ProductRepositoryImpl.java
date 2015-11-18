package model.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import model.dataobjects.HierarchyNode;
import model.dataobjects.Image;
import model.dataobjects.Product;
import model.dataobjects.User;
import model.dataobjects.supporting.ObjectList;
import model.persistence.queryParameters.ProductSearch;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProductRepositoryImpl implements ProductRepositoryCustom{

	private static final Logger logger = LoggerFactory.getLogger(ProductRepositoryImpl.class);		
	
	@Autowired
	private ImageRepository imageRepository;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private EntityManager entityManager;

	@Override
	public Product addImage(Product product, Image image) {
		if (product.getImages()==null || product.getImages().size()<=0){
			image.setMain(true);
		} else {
			image.setMain(false);
		}
		imageRepository.save(image);
		product.addImage(image);	
		productRepository.save(product);
		return product;
	}

	@Override
	public Product addImage(Product product, Collection<Image> images) {
		imageRepository.save(images);		
		for (Image image: images) {
			if (product.getImages()==null || product.getImages().size()<=0){
				image.setMain(true);
			} else {
				image.setMain(false);
			}
			product.addImage(image);
		}
		productRepository.save(product);
		return product;
	}

	@Override
	public boolean removeImage(Product product, Long imageId) {
		boolean isMain = false;
		boolean exists = false;
		Iterator<Image> iterator = product.getImages().iterator();
		while (iterator.hasNext()){
			Image image = iterator.next();
			if (image.getId()!=null && image.getId().equals(imageId)){
				exists = true;
				isMain = image.isMain();
				iterator.remove();
								
			}
		}
		
		if (isMain && product.getImages()!=null && product.getImages().size()>0){
			product.getImages().get(0).setMain(true);
			imageRepository.save(product.getImages().get(0));
		}
				
		if (!exists){			
			return false;
		} else {
			imageRepository.delete(imageId);
			productRepository.save(product);
			return true;
		}
	}


	@Override
	public ObjectList<Product> searchProduct(ProductSearch search) {

		ObjectList<Product> wrapper = new ObjectList<Product>();
		
		if (search==null){
			return wrapper;
		}
		String hql = "";
		
		hql = hql + "select p from Product p LEFT JOIN p.images LEFT JOIN p.owners owners LEFT JOIN p.ownersOtherLanguage ownersOtherLanguage";
		//hql = hql + "select p from Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.owners owners";
		
		if (search.getCategoryValues()!=null && search.getCategoryValues().size()>0){
			hql = hql + " INNER JOIN p.categoryValues categoryValues ";
		}
		
		hql = hql + " where ";
		
		boolean needsAnd = false;
		
		if (search.getSearchTerm()!=null){			
			if (needsAnd){ hql = hql + " AND ";}
			hql = hql + ""
					+ "( "
					+ "		lower(p.description) LIKE lower(CONCAT('%',:search,'%')) OR "
					+ "		lower(p.name) LIKE lower(CONCAT('%',:search,'%'))  "
					+ ") ";
			needsAnd = true;
		}
		if (search.getHierarchy()!=null){		
			if (needsAnd){ hql = hql + " AND ";}
			hql = hql + "(p.hierarchyPlacement.lineage LIKE CONCAT(:lineage,'%')) ";
			needsAnd = true;
		}
		if (search.getCategoryValues()!=null && search.getCategoryValues().size()>0){
			if (needsAnd){ hql = hql + " AND ";}
			hql = hql + "( categoryValues IN (:categoryValues) ) ";
			needsAnd = true;			
		}
		
		if (search.getWithImages()!=null){
			if (needsAnd){ hql = hql + " AND ";}			
			if (search.getWithImages()){
				hql = hql + "(p.images IS NOT EMPTY ) ";
			} else {
				hql = hql + "(p.images IS EMPTY ) ";
			}
			needsAnd = true;
		}
		
		if (search.getOwned()!=null && search.getOwner()!=null){
			if (needsAnd){ hql = hql + " AND ";}
			if (search.getOwned()){
				hql = hql + "((:owned) IN owners OR (:owned) IN ownersOtherLanguage) ";
			} else {
				hql = hql + "((p.owners IS EMPTY OR (:owned) NOT IN owners) AND (p.ownersOtherLanguage IS EMPTY OR (:owned) NOT IN ownersOtherLanguage))";			
			}
			
			needsAnd = true;
		}
		
		if (search.getWithPrice()!=null){
			if (needsAnd){ hql = hql + " AND ";}			
			if (search.getWithPrice()){
				hql = hql + "(p.minPrice IS NOT NULL ) ";
			} else {
				hql = hql + "(p.minPrice IS NULL ) ";
			}
			needsAnd = true;			
		}

		if (search.getCategoryValues()!=null && search.getCategoryValues().size()>0){
			hql = hql + "GROUP BY p having count(categoryValues)=:sizeCategoryValues "; 			
		}
		
		if (search.getSortBy()!=null){
			if (search.getSortBy().toLowerCase().equals("name")){
				hql = hql + " ORDER BY p.name";
			} else if (search.getSortBy().toLowerCase().equals("price")){
				hql = hql + " ORDER BY p.minPrice";
			} else {
				hql = hql + " ORDER BY p.id";
			}
		} else {
			hql = hql + " ORDER BY p.id";
		}
		
		if (search.getSortOrder()!=null){
			if ("asc".equals(search.getSortOrder().toLowerCase())){
				hql = hql + " ASC";
			} else if ("desc".equals(search.getSortOrder().toLowerCase())){
				hql = hql + " DESC";
			} else {
				hql = hql + " DESC";
			}
		} else {
			hql = hql + " DESC";
		}
			
		logger.info("Executing HQL: "+ hql);
		
		TypedQuery<Product> query = entityManager.createQuery(hql, Product.class);
		
		if (search.getSearchTerm()!=null){ query.setParameter("search",search.getSearchTerm());}
		if (search.getHierarchy()!=null){ query.setParameter("lineage",search.getHierarchy().getLineage());}
		if (search.getCategoryValues()!=null && search.getCategoryValues().size()>0) {
				query.setParameter("categoryValues", search.getCategoryValues()); 
				query.setParameter("sizeCategoryValues",search.getCategoryValues().size());
		}
		if (search.getOwned()!=null && search.getOwner()!=null){
			Set<User> setUsers = new HashSet<>();
			setUsers.add(search.getOwner());
			query.setParameter("owned",setUsers); 
		}
		
		
		List<Product> result = new ArrayList<>();	
		
		
		if (search.getMaxResults()!=null && search.getPage()!=null) {
			
			logger.info("Obtaining results - Max results: " + search.getMaxResults());
			logger.info("Obtaining results - Starting in : " + search.getPage()*search.getMaxResults());
			
			query.setMaxResults(search.getMaxResults()+1);
			query.setFirstResult(search.getPage()*search.getMaxResults());
			
		} else {
			logger.info("Skipping setting paginated results");
		}

		List<Product> listFromDB = query.getResultList();
		if (listFromDB!=null){
			if (search.getMaxResults() !=null & search.getPage()!=null && (listFromDB.size() == search.getMaxResults()+1)){
				listFromDB.remove(listFromDB.size()-1);
				wrapper.setHasNext(true);
			} else {
				wrapper.setHasNext(false);
			}
			
			
			logger.info("Obtained "+ listFromDB.size() + " products");
			
			wrapper.setObjects(listFromDB);
			if (search.getMaxResults()!=null && search.getPage()!=null) {
				wrapper.setMaxResults(search.getMaxResults());	
			}
		}
		return wrapper;
	}

	@Override
	public Collection<Product> searchProduct(HierarchyNode node) {
		ProductSearch search = new ProductSearch();
		search.setHierarchy(node);
		Collection<String> errors = search.errors();
		ObjectList<Product> searchResult = searchProduct(search);	
		return searchResult.getObjects();
	}

	@Override
	public void saveWithImages(Collection<Product> products, Collection<Image> images) {
		imageRepository.save(images);
		productRepository.save(products);		
	}
	
	@Override
	public void saveWithImages(Product products, Collection<Image> images) {
		imageRepository.save(images);
		productRepository.save(products);		
	}

	@Override
	public boolean mergeAndSaveProductWithoutImages(Product product,Collection<Image> images) {

		product = productRepository.findOne(product.getId());
		if (product!=null && product.getImages()!=null && product.getImages().size()<=0){
			productRepository.addImage(product, images);
			return true;
		}		
		return false;
		
	}


	
	
	

}
