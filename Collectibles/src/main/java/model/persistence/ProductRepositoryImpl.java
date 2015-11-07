package model.persistence;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;

import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.Image;
import model.dataobjects.Product;

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
			System.out.println("Not exists");
			return false;
		} else {
			imageRepository.delete(imageId);
			productRepository.save(product);
			return true;
		}
	}


	@Override
	public List<Product> searchProduct(HierarchyNode node, String search, Collection<CategoryValue> categoryValues, Boolean withImages) {
		String hql = "";
		
		hql = hql + "select p from Product p LEFT JOIN FETCH p.images ";
		
		if (categoryValues!=null){
			hql = hql + " INNER JOIN p.categoryValues categoryValues ";
		}
		
		hql = hql + " where ";
		
		boolean needsAnd = false;
		
		if (search!=null){			
			if (needsAnd){ hql = hql + " AND ";}
			hql = hql + ""
					+ "( "
					+ "		lower(p.description) LIKE lower(CONCAT('%',:search,'%')) OR "
					+ "		lower(p.name) LIKE lower(CONCAT('%',:search,'%'))  "
					+ ") ";
			needsAnd = true;
		}
		if (node!=null){		
			if (needsAnd){ hql = hql + " AND ";}
			hql = hql + "(p.hierarchyPlacement.lineage LIKE CONCAT(:lineage,'%')) ";
			needsAnd = true;
		}
		if (categoryValues!=null){
			if (needsAnd){ hql = hql + " AND ";}
			hql = hql + "( categoryValues IN (:categoryValues) ) ";
			needsAnd = true;			
		}
		
		if (withImages!=null){
			if (needsAnd){ hql = hql + " AND ";}			
			if (withImages){
				hql = hql + "(p.images IS NOT EMPTY )";
			} else {
				hql = hql + "(p.images IS EMPTY )";
			}
			needsAnd = true;
		}
		
		
		if (categoryValues!=null){
			hql = hql + "GROUP BY p having count(categoryValues)=:sizeCategoryValues "; 			
		}
		
		
		
		TypedQuery<Product> query = entityManager.createQuery(hql, Product.class);
		
		if (search!=null){ query.setParameter("search",search);}
		if (node!=null){ query.setParameter("lineage",node.getLineage());}
		if (categoryValues!=null) {
				query.setParameter("categoryValues", categoryValues); 
				query.setParameter("sizeCategoryValues",categoryValues.size());
		}
		
		System.out.println(query.toString());
		
		return query.getResultList();
	}

	@Override
	public List<Product> searchProduct(HierarchyNode node) { 
		return  searchProduct(node, null, null, null);
	}

	@Override
	public List<Product> searchProduct(HierarchyNode node, String search) {
		return  searchProduct(node, search, null, null);
	}

	@Override
	public List<Product> searchProduct(String search) {
		return  searchProduct(null, search, null, null);
	}



	
	
	

}
