package hr.hrg.hipster.sql;

import java.util.*;

@SuppressWarnings("rawtypes")
public class ColumnSet<C extends BaseColumnMeta> implements Iterable<C>{

	protected long items;
	protected C[] all;
	protected BaseEntityMeta<?,?,C> meta;
	
	public ColumnSet(BaseEntityMeta<?,?,C> meta){
		this(meta, false);
	}

	public ColumnSet(BaseEntityMeta<?,?,C> meta, boolean allElements){
		this.meta = meta;
		this.all = (C[]) meta.columnArray;
		if(allElements){
			items = -1L >>> -all.length;
		}
	}
	
	protected ColumnSet(long items, C[] all, BaseEntityMeta<?, ?, C> meta) {
		this.items = items;
		this.all = all;
		this.meta = meta;
	}

	public int size() {
		return Long.bitCount(items);
	}

	public boolean isEmpty() {
		return items == 0;
	}

	public final boolean contains(C column) {
		if(column == null) return false;
		return contains(column.ordinal);
	}

	public final boolean contains(int ordinal) {
		return (items & (1L << ordinal)) != 0;
	}

	public boolean contains(String columnName){
		BaseColumnMeta<?> column = meta.getColumn(columnName);
		return column == null ? false : contains(column.ordinal);
	}
	
	@Override
	public Iterator<C> iterator() {
		return new ColumnsIterator();
	}

	public boolean add(C column){
        long old = items;
        items |= (1L << column.ordinal());
        return items != old;	
	}

	@SuppressWarnings("unchecked")
	public boolean add(String columnName){
		BaseColumnMeta<?> column = meta.getColumn(columnName);

        return add((C) column);	
	}
	
	public final boolean remove(C column) {
		if(column == null) return false;

		return remove(column.ordinal);
	}

	public final boolean remove(int ordinal) {
        long old = items;
        
        items &= ~(1L << ordinal);
        
        return items != old;	
	}

	public int getAllColumnsCount(){
		return all.length;
	}

	public C getColumn(int ordinal){
		return contains(ordinal) ? all[ordinal] : null;
	}

	public C getColumn(String columnName){
		BaseColumnMeta<?> column = meta.getColumn(columnName);
		return column == null ? null : getColumn(column.ordinal);
	}

	public void clear() {
		items = 0;
	}

	@Override
	public ColumnSet<C> clone(){
		return new ColumnSet<>(items, all, meta);
	}
	
	public void addAll(ColumnSet<C> other){
		items |= other.items;
	}

	public void removeAll(ColumnSet<C> other){
		items &= ~other.items;
	}
	
	private class ColumnsIterator implements Iterator<C> {
        long left;
        long last = 0;

        ColumnsIterator() {
            this.left = items;
        }

        public boolean hasNext() {
            return left != 0;
        }

        @SuppressWarnings("unchecked")
        public C next() {
            if (left == 0) throw new NoSuchElementException();

            last = left & -left;
            left -= last;
            
            return (C) all[Long.numberOfTrailingZeros(last)];
        }

        public void remove(){
            if (last == 0) throw new IllegalStateException("You can only remove item once form iterator");
            
            items &= ~last;

            last = 0;
        }
    }	
	
}
