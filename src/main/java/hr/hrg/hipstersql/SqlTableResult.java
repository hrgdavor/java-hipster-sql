package hr.hrg.hipstersql;

import java.util.List;
import java.util.Map;

public class SqlTableResult {
    private int offset;
    private int limit;
    private int rowcount;
    private List<Map<Object, Object>> data;
    
    public SqlTableResult(int offset, int limit, int rowcount, List<Map<Object, Object>> data) {
        this.offset = offset;
        this.limit = limit;
        this.rowcount = rowcount;
        this.data = data;
    }

    public SqlTableResult(List<Map<Object, Object>> data) {
    	this.data = data;
    	this.offset = 0;
    	this.rowcount = data.size();
    	this.limit = this.rowcount;
	}

	public int getOffset() {
        return offset;
    }
    public int getLimit() {
        return limit;
    }
    public int getRowcount() {
        return rowcount;
    }
    public List<Map<Object, Object>> getData() {
        return data;
    }
    public void setOffset(int offset) {
        this.offset = offset;
    }
    public void setLimit(int limit) {
        this.limit = limit;
    }
    public void setRowcount(int rowcount) {
        this.rowcount = rowcount;
    }
    public void setData(List<Map<Object, Object>> data) {
        this.data = data;
    }

    
    
}
