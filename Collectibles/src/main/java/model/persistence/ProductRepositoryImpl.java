package model.persistence;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;

import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.Image;
import model.dataobjects.Product;
import model.persistence.queryParameters.ProductSearch;

public class ProductRepositoryImpl implements ProductRepositoryCustom{

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
	public Collection<Product> searchProduct(ProductSearch search) {
		String hql = "";
		
		hql = hql + "select p from Product p LEFT JOIN FETCH p.images ";
		
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
		
		if (search.getOwned()!=null){
			if (needsAnd){ hql = hql + " AND ";}			
			hql = hql + "(p.owned = :owned ) ";			
			needsAnd = true;
		}
		
		
		if (search.getCategoryValues()!=null && search.getCategoryValues().size()>0){
			hql = hql + "GROUP BY p having count(categoryValues)=:sizeCategoryValues "; 			
		}
		
		
		
		TypedQuery<Product> query = entityManager.createQuery(hql, Product.class);
		
		if (search.getSearchTerm()!=null){ query.setParameter("search",search.getSearchTerm());}
		if (search.getHierarchy()!=null){ query.setParameter("lineage",search.getHierarchy().getLineage());}
		if (search.getCategoryValues()!=null && search.getCategoryValues().size()>0) {
				query.setParameter("categoryValues", search.getCategoryValues()); 
				query.setParameter("sizeCategoryValues",search.getCategoryValues().size());
		}
		if (search.getOwned()!=null) { query.setParameter("owned",search.getOwned()); }
		
		System.out.println(hql);
		
		Set<Product> result = new HashSet<>();
		result.addAll(query.getResultList());
		return result;
	}

	@Override
	public Collection<Product> searchProduct(HierarchyNode node) {
		ProductSearch search = new ProductSearch();
		search.setHierarchy(node);
		Collection<String> errors = search.errors();
		return searchProduct(search);		
	}


	
	
	

}
