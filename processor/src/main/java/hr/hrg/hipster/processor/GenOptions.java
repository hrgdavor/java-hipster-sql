package hr.hrg.hipster.processor;

import static hr.hrg.hipster.sql.BooleanEnum.*;

import hr.hrg.hipster.entity.*;
import hr.hrg.hipster.sql.*;

public class GenOptions {
	BooleanEnum genJson     = DEFAULT;
	BooleanEnum genMeta     = DEFAULT;
	BooleanEnum genUpdate   = DEFAULT;
	BooleanEnum genBuilder  = DEFAULT;
	BooleanEnum genVisitor  = DEFAULT;
	GenOptions parent;
	
	public GenOptions(GenOptions parent, HipsterEntity ano) {
		this.parent = parent;
		if(ano != null) {			
			genJson = ano.genJson();
			genMeta = ano.genMeta();
			genVisitor = ano.genVisitor();
			genUpdate = ano.genUpdate();
			genBuilder = ano.genBuilder();
		}
	}
	
	public GenOptions(boolean genJson, boolean genMeta, boolean genVisitor, boolean genUpdate, boolean genBuilder){
		this(null, null);
		this.genJson     = genJson     ? TRUE:FALSE;
		this.genMeta     = genMeta     ? TRUE:FALSE;
		this.genVisitor  = genVisitor  ? TRUE:FALSE;
		this.genUpdate   = genUpdate   ? TRUE:FALSE;
		this.genBuilder  = genBuilder  ? TRUE:FALSE;
	}

	public boolean isGenJson() {
		return genJson == DEFAULT ? parent.isGenJson() : genJson == TRUE;
	}
	
	public boolean isGenMeta() {
		return genMeta == DEFAULT ? parent.isGenMeta() : genMeta == TRUE;
	}
	
	public boolean isGenVisitor() {
		return genVisitor == DEFAULT ? parent.isGenVisitor() : genVisitor == TRUE;
	}
	
	public boolean isGenUpdate() {
		return genUpdate == DEFAULT ? parent.isGenUpdate() : genUpdate == TRUE;
	}
	
	public boolean isGenBuilder() {
		return genBuilder == DEFAULT ? parent.isGenBuilder() : genBuilder == TRUE;
	}
}
