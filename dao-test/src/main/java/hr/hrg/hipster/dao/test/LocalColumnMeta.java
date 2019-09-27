package hr.hrg.hipster.dao.test;

import java.lang.annotation.*;

import hr.hrg.hipster.entity.*;
import hr.hrg.hipster.sql.*;
import hr.hrg.hipster.type.*;

public class LocalColumnMeta<T> extends ColumnMeta<T>{

	public LocalColumnMeta(
			int ordinal, 
			String _name, 
			String _columnName, 
			String _getterName, 
			IEntityMeta meta,
			Class<T> _type, 
			Class<?> _primitiveType, 
			String _columnSql, ICustomType<?> typeHandler,
			Class<?> ...typeParams) {
		super(ordinal, _name, _columnName, _getterName, meta, _type, _primitiveType, _columnSql, typeHandler, typeParams);
	}

	@Override
	public LocalColumnMeta<T> withAnnotations(Annotation... annotations){
		super.withAnnotations(annotations);
		return this;
	}
}
