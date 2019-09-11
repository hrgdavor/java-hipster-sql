package hr.hrg.hipster.sql;

public interface Key<K> {

	public Class<K> getType();
	public int ordinal();
}
