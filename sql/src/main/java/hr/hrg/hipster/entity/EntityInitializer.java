package hr.hrg.hipster.entity;

public interface EntityInitializer<T> {
	public boolean isEntityInitialized(T entity);
	public void initEntity(T entity);
}
