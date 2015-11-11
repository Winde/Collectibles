package model.dataobjects.supporting;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonView;

public class ObjectList<E> {

	public interface ObjectListView{};
	
	@JsonView(ObjectListView.class)
	private Collection<E> objects = null;
	
	@JsonView(ObjectListView.class)
	private Integer maxResults = null;

	@JsonView(ObjectListView.class)
	private Boolean hasNext = Boolean.FALSE;
	
	
	public Collection<E> getObjects() {
		return objects;
	}

	public void setObjects(Collection<E> objects) {
		this.objects = objects;
	}

	public Integer getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}

	public Boolean getHasNext() {
		return hasNext;
	}

	public void setHasNext(Boolean hasNext) {
		this.hasNext = hasNext;
	}

}
