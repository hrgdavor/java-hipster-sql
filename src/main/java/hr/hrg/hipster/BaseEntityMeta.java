package hr.hrg.hipster;

import hr.hrg.hipster.dao.IEntityMeta;
import hr.hrg.hipster.sql.IColumnMeta;

public abstract class BaseEntityMeta<T,ID,E extends IColumnMeta> implements IEntityMeta<T,ID,E>{
	private final int ordinal;

	public BaseEntityMeta(int ordinal) {
		this.ordinal = ordinal;
	}

	@Override
	public final int ordinal() {
		return ordinal;
	}
	  
}
