package model.persistence;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import model.dataobjects.HierarchyNode;
import model.dataobjects.Image;
import model.dataobjects.Price;
import model.dataobjects.Product;
import model.dataobjects.User;
import model.dataobjects.supporting.ObjectList;
import model.persistence.queryParameters.ProductSearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
		product.addImage(image);
		imageRepository.save(image);
		productRepository.save(product);
		return product;
	}

	@Override
	public Product addImage(Product product, Collection<Image> images) {			
		for (Image image: images) {
			if (product.getImages()==null || product.getImages().size()<=0){
				image.setMain(true);
			} else {
				image.setMain(false);
			}
			product.addImage(image);
		}
		imageRepository.save(images);	
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
			logger.info("Search object is null");
			return wrapper;
		}
		String hql = "";
		
		hql = hql + "select distinct p from Product p LEFT JOIN p.images ";
		if ((search.getUsersWhoOwn()!=null && search.getUsersWhoOwn().size()>0) || (search.getUsersWhoDontOwn()!=null && search.getUsersWhoDontOwn().size()>0)){			
			hql = hql + " LEFT JOIN p.owners owners ";				
			hql = hql + " LEFT JOIN p.ownersOtherLanguage ownersOtherLanguage ";
		}
		if (search.getWishers()!=null && search.getWishers().size()>0){	
			hql = hql + " LEFT JOIN p.wishers wishers ";		
		}
		if (search.getStore()!=null || search.getSeller()!=null){
			hql = hql + " LEFT JOIN p.prices prices with ";
			if (search.getStore()!=null){
				hql = hql + " prices.connectorName = :store ";
			}
			if (search.getStore()!=null && search.getSeller()!=null){
				hql = hql + " AND ";
			}
			if (search.getSeller()!=null){
				hql = hql + " prices.seller = :seller ";
			}
		}
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
		
		if (search.getWishers()!=null && search.getWishers().size()>0){			
			for (int i=0;i<search.getWishers().size();i++){
				if (needsAnd){ hql = hql + " AND ";}
				hql = hql + "((:wishers"+i+") IN elements(p.wishers)) ";
				needsAnd = true;
			}						
		}
		
		if (search.getUsersWhoOwn()!=null && search.getUsersWhoOwn().size()>0){			
			for (int i=0;i<search.getUsersWhoOwn().size();i++){
				if (needsAnd){ hql = hql + " AND ";}
				hql = hql + "((:owners"+i+") IN elements(p.owners) OR (:owners"+i+") IN elements(p.ownersOtherLanguage)) ";
				needsAnd = true;
			}						
		}
		
		if (search.getUsersWhoDontOwn()!=null && search.getUsersWhoDontOwn().size()>0){
			for (int i=0;i<search.getUsersWhoDontOwn().size();i++){
				if (needsAnd){ hql = hql + " AND ";}
				hql = hql + "((p.owners IS EMPTY OR (:usersWhoDontOwn"+i+") NOT IN elements(p.owners)) AND (p.ownersOtherLanguage IS EMPTY OR (:usersWhoDontOwn"+i+") NOT IN elements(p.ownersOtherLanguage)))";
				needsAnd = true;
			}			
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
		
		if (search.getStore()!=null){
			if (needsAnd){ hql = hql + " AND ";}
			hql = hql + " ( :store in (SELECT price.connectorName from Price price WHERE price.product = p) ) ";
			needsAnd = true;
		}
		if (search.getSeller()!=null){
			if (needsAnd){ hql = hql + " AND ";}
			hql = hql + " ( :seller in (SELECT price.seller from Price price WHERE price.product = p) )  ";
			needsAnd = true;
		}
		
		if (search.getStore()!=null || search.getSeller()!=null){
			if (needsAnd){ hql = hql + " AND ";}
			hql = hql + " prices.price IS NOT NULL  ";
			needsAnd = true;
		}

		if (search.getStore()!=null || search.getSeller()!=null){
			if (needsAnd){ hql = hql + " AND ";}
			hql = hql + " (prices.price = (SELECT min(prices.price) from prices where prices.product = p AND prices.connectorName = :store)) ";
			needsAnd = true;
		}
		
		if (search.getCategoryValues()!=null && search.getCategoryValues().size()>0){
			hql = hql + "GROUP BY p having count(categoryValues)=:sizeCategoryValues "; 			
		}
		
		
		
		
		if (search.getSortBy()!=null){
			if (search.getSortBy().toLowerCase().equals("name")){
				hql = hql + " ORDER BY p.name";
			} else if (search.getSortBy().toLowerCase().equals("price") && search.getStore()==null && search.getSeller()==null){
				hql = hql + " ORDER BY p.minPrice";
			} else if (search.getSortBy().toLowerCase().equals("price")){
				hql = hql + " ORDER BY prices.price";	
			} else if (search.getSortBy().toLowerCase().equals("rating")){
				hql = hql + " ORDER BY p.mainRating";			
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
				hql = hql + " ASC";
			}
		} else {
			hql = hql + " ASC";
		}
			
		logger.info("Executing HQL: "+ hql);
		
		TypedQuery<Product> query = entityManager.createQuery(hql, Product.class);
		
		if (search.getSearchTerm()!=null){ query.setParameter("search",search.getSearchTerm());}
		if (search.getHierarchy()!=null){ query.setParameter("lineage",search.getHierarchy().getLineage());}
		if (search.getCategoryValues()!=null && search.getCategoryValues().size()>0) {
				query.setParameter("categoryValues", search.getCategoryValues()); 
				query.setParameter("sizeCategoryValues",search.getCategoryValues().size());
		}
		if (search.getWishers()!=null && search.getWishers().size()>0){			
			int i=0;
			for (User user: search.getWishers()){
				query.setParameter("wishers"+i,user);
				i=i+1;
			}
		}
		if (search.getUsersWhoOwn()!=null && search.getUsersWhoOwn().size()>0){			
			int i=0;
			for (User user: search.getUsersWhoOwn()){
				query.setParameter("owners"+i,user);
				i=i+1;
			}
		}
		
		if (search.getUsersWhoDontOwn()!=null && search.getUsersWhoDontOwn().size()>0){
			int i=0;
			for (User user: search.getUsersWhoDontOwn()){
				query.setParameter("usersWhoDontOwn"+i,user);
				i=i+1;
			}
		}
		if (search.getStore()!=null){
			query.setParameter("store", search.getStore());
		}
		if (search.getSeller()!=null){
			query.setParameter("seller", search.getSeller());
		}	
		
		
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
			
			if (search.getStore()!=null || search.getSeller()!=null){
				for (Product product : listFromDB){
					SortedSet<Price> prices = product.getPrices();
					boolean found = false;
					Long currentMinPrice = null;
					for (Price price : prices){
						if (
								(search.getStore()==null || search.getStore().equals(price.getConnectorName()))
								&&
								(search.getSeller()==null || search.getSeller().equals(price.getSeller()))
								&&
								(currentMinPrice == null || (price.getPrice()!=null && price.getPrice()<currentMinPrice))
							){
							product.setMinPrice(price.getPrice());
							product.setMinPriceCurrency(price.getCurrency());
							product.setMinPriceLink(price.getLink());
							product.setMinPriceSeller(price.getSeller());		
							currentMinPrice = price.getPrice();
							found = true;
						}
					}
					if (!found){
						product.setMinPrice(null);
						product.setMinPriceCurrency(null);
						product.setMinPriceLink(null);
						product.setMinPriceSeller(null);
					}
				}
			}
			
			if (search.getMaxResults() !=null & search.getPage()!=null && (listFromDB.size() == search.getMaxResults()+1)){
				listFromDB.remove(listFromDB.size()-1);
				wrapper.setHasNext(true);
			} else {
				wrapper.setHasNext(false);
			}
			
			
			logger.info("Obtained "+ listFromDB.size() + " products");
			
			//Set<Product> result = new LinkedHashSet<>(listFromDB);
			
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

	@Override
	public Collection<Product> findByConnectorReference(String connector, String reference) {
 
		String hql = "";
		
		hql = "select distinct p from Product p LEFT JOIN FETCH p.owners "
				+ "WHERE p.connectorReferences[:connector] = :reference ";				
				
		
		TypedQuery<Product> query = entityManager.createQuery(hql, Product.class);
		query.setParameter("reference", reference);
		query.setParameter("connector", connector);
		return query.getResultList();
	}

	@Override
	public Product findOneRefreshed(Long id) {
		Product product = entityManager.find(Product.class, id);
		if (product!=null)
		entityManager.refresh(product);
		return product;
	}


	
	
	

}
