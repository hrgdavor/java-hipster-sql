package hr.hrg.hipster.dao.test;

import java.sql.*;
import java.util.*;

import hr.hrg.hipster.sql.*;

public class User1Meta extends BaseEntityMeta<User1, Long, LocalColumnMeta> {
  private static final Class<User1> ENTITY_CLASS = User1.class;
  
  public final LocalColumnMeta<Long> id;
  public final LocalColumnMeta<List> name;
  public final LocalColumnMeta<Integer> age;

  private final ImmutableList<BaseColumnMeta> _columns;

  public static final int COLUMN_COUNT = 3;

  private static final ImmutableList<String> COLUMN_NAMES = ImmutableList.safe("user_id","name","age");

  private static final String[] COLUMN_ARRAY_SORTED_STR = {"age","id","name"};


  public User1Meta(HipsterSql hipster, int ordinal) {
	  super(ordinal, "user_table", ENTITY_CLASS);
  
    _typeHandler = new ICustomType<?>[COLUMN_COUNT];

	if(hipster != null){
		TypeSource _typeSource = hipster.getTypeSource();
		_typeHandler[0] = (ICustomType<Long>) _typeSource.getForRequired(Long.class);
		_typeHandler[1] = (ICustomType<List<String>>) _typeSource.getInstanceRequired(StringListGetter.class);
		_typeHandler[2] = (ICustomType<Integer>) _typeSource.getForRequired(Integer.class);
	}
	
	id = new LocalColumnMeta<Long>(0, "id","user_id","getId",this,Long.class,null,"", _typeHandler[0]);
    name = new LocalColumnMeta<List>(1, "name","name","getName",this,List.class,null,"", _typeHandler[1],String.class);
    age = new LocalColumnMeta<Integer>(2, "age","age","getAge",this,Integer.class,int.class,"", _typeHandler[2]).withAnnotations();
    
	this._columnArray = new LocalColumnMeta[]{id,name,age};
	this._columns =  ImmutableList.safe((BaseColumnMeta[])_columnArray);
	this._columnArraySorted = new LocalColumnMeta[]{age,id,name};
	this._columnArraySortedStr = COLUMN_ARRAY_SORTED_STR;
	this._columnCount = COLUMN_COUNT;

  }
  
  public final User1 fromResultSet(ResultSet rs) throws SQLException {
    Long id = rs.getLong(1);
    List<String> name = (List<String>)_typeHandler[1].get(rs,2);
    int age = rs.getInt(3);

    return new User1Immutable(
    id, name, age);}

  @Override
  public final UserUpdate mutableCopy(Object v) {
    return new UserUpdate((User)v);
  }

  @Override
  public final String getEntityName() {
    return "User";
  }

  public final ImmutableList<String> getColumnNames() {
    return COLUMN_NAMES;
  }

  public final boolean containsColumn(String columnName) {
    return Arrays.binarySearch(COLUMN_ARRAY_SORTED_STR, columnName) > -1;
  }

  @Override
  public final ImmutableList<BaseColumnMeta> getColumns() {
    return _columns;
  }

  @Override
  public final BaseColumnMeta getPrimaryColumn() {
    return id;
  }

  @Override
  public final Long entityGetPrimary(User1 instance) {
    return instance.getId();
  }
}
