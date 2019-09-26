package hr.hrg.hipster.dao;

import java.util.*;

import hr.hrg.hipster.sql.*;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class EntityCopy {
	int[] indexFrom;
	int[] indexTo;
	
	
	private EntityCopy(){}

	private EntityCopy(List<Integer> indexFrom, List<Integer> indexTo){
		this.indexFrom = new int[indexFrom.size()];
		this.indexTo = new int[indexTo.size()];
		
		for(int i=0;i<this.indexFrom.length;i++) {
			this.indexFrom[i] = indexFrom.get(i);
			this.indexTo[i] = indexTo.get(i);
		}
	}
	
	
	public void copy(IEnumGetter from, IUpdatable to) {
		for(int i=0;i<this.indexFrom.length;i++) {
			to.setValue(this.indexTo[i], from.getValue(this.indexFrom[i]));
		}
	}
	
	public Builder builder(IEntityMeta<?,?> metaFrom, IEntityMeta<?,?> metaTo) {
		return new Builder(metaFrom, metaTo);

	}
	
	public static class Builder{
		private IEntityMeta<?, ?> metaFrom;
		private IEntityMeta<?, ?> metaTo;
		boolean[] ignored;
		List<Integer> indexFrom = new ArrayList<>();
		List<Integer> indexTo = new ArrayList<>();

		public Builder(IEntityMeta<?,?> metaFrom, IEntityMeta<?,?> metaTo) {
			this.metaFrom = metaFrom;
			this.metaTo = metaTo;
			ignored = new boolean[metaFrom.getColumnCount()];
		}

		/** add all columns by name(respecting ignored columns)
		 * 
		 * @param columnNames column names 
		 * @return self
		 */
		public Builder byName(String ...columnNames) {

			for (String columnName : columnNames) {
				int idxFrom = metaFrom.getColumnOrdinal(columnName); 
				if(ignored[idxFrom]) continue;
				
				int idxTo = metaTo.getColumnOrdinal(columnName);
				if(idxFrom != -1 && idxTo != -1) {
					indexFrom.add(idxFrom);
					indexTo.add(idxTo);
				}
			}
			return this;
		}
		
		/** add all columns with same name(respecting ignored columns)
		 * 
		 * @return self
		 */
		public Builder sameNames() {
			List<ColumnMeta> columns = (List<ColumnMeta>) metaFrom.getColumns();
			for (ColumnMeta columnFrom : columns) {
				if(ignored[columnFrom.ordinal()]) continue;
				
				ColumnMeta columnTo = metaTo.getColumn(columnFrom.name());
				if (columnTo != null) {
					indexFrom.add(columnFrom.ordinal());
					indexTo.add(columnTo.ordinal());
				}
			}
			return this;
		}		

		public Builder ignore(ColumnMeta ...columns) {
			for (ColumnMeta column: columns) {
				if(column.getEntity() != metaFrom.getEntityClass()) {
					throw new RuntimeException("Can not use column from different entity. Column from: "+column.getEntity() +" but metadata from: "+ metaFrom.getEntityClass());
				}
				ignored[column.ordinal()] = true;
			}
			return this;
		}
		
		public<T> Builder addMapping(ColumnMeta<T> columnFrom, ColumnMeta<T> columnTo) {
			if(columnFrom.getEntity() != metaFrom.getEntityClass()) {
				throw new RuntimeException("Can not use column from different entity. Column from: "+columnFrom.getEntity() +" but metadata from: "+ metaFrom.getEntityClass());
			}
			if(columnTo.getEntity() != metaTo.getEntityClass()) {
				throw new RuntimeException("Can not use column from different entity. Column from: "+columnTo.getEntity() +" but metadata from: "+ metaTo.getEntityClass());
			}
			indexFrom.add(columnFrom.ordinal());
			indexTo.add(columnTo.ordinal());
			return this;
		}
		
		public EntityCopy build() {
			return new EntityCopy(indexFrom, indexTo);
		}
		
	}
}
