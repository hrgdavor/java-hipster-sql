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
	BooleanEnum genSql  	= DEFAULT;
	BooleanEnum genMongo  	= DEFAULT;
	BooleanEnum genAnnotations	= DEFAULT;
	BooleanEnum mongoSkipNull 	= DEFAULT;
	BooleanEnum mongoUseFieldName 	= DEFAULT;
	BooleanEnum inspectChange 	= DEFAULT;
	GenOptions parent;
	
	public GenOptions(GenOptions parent, HipsterEntity ano) {
		this.parent = parent;
		if(ano != null) {			
			genJson = ano.genJson();
			genMeta = ano.genMeta();
			genVisitor = ano.genVisitor();
			genUpdate = ano.genUpdate();
			inspectChange = ano.inspectChange();
			genBuilder = ano.genBuilder();
			genMongo = ano.genMongo();
			genSql = ano.genSql();
			mongoSkipNull = ano.mongoSkipNull();
			mongoUseFieldName = ano.mongoUseFieldName();
		}
	}
	
	public GenOptions(boolean genAnno, boolean genJson, boolean genMeta, boolean genVisitor, boolean genUpdate, boolean genBuilder, boolean genSql, boolean genMongo, boolean mongoSkipNull, boolean mongoUseFieldName){
		this(null, null);
		this.genAnnotations = genAnno ? TRUE:FALSE;
		
		this.genJson     = genJson     ? TRUE:FALSE;
		this.genMeta     = genMeta     ? TRUE:FALSE;
		this.genVisitor  = genVisitor  ? TRUE:FALSE;
		this.genUpdate   = genUpdate   ? TRUE:FALSE;
		this.genBuilder  = genBuilder  ? TRUE:FALSE;
		this.genMongo    = genMongo    ? TRUE:FALSE;
		this.genSql      = genSql      ? TRUE:FALSE;
		this.mongoSkipNull      = mongoSkipNull      ? TRUE:FALSE;
		this.mongoUseFieldName  = mongoUseFieldName  ? TRUE:FALSE;
	}

	public boolean isGenJson() {
		return genJson == DEFAULT && parent != null ? parent.isGenJson() : genJson == TRUE;
	}
	
	public boolean isGenMeta() {
		return genMeta == DEFAULT && parent != null  ? parent.isGenMeta() : genMeta == TRUE;
	}
	
	public boolean isGenVisitor() {
		return genVisitor == DEFAULT && parent != null  ? parent.isGenVisitor() : genVisitor == TRUE;
	}
	
	public boolean isGenUpdate() {
		return genUpdate == DEFAULT && parent != null  ? parent.isGenUpdate() : genUpdate == TRUE;
	}
	
	public boolean isInspectChange() {
		return inspectChange == DEFAULT && parent != null  ? parent.isInspectChange() : inspectChange == TRUE;
	}
	
	public boolean isGenBuilder() {
		return genBuilder == DEFAULT && parent != null  ? parent.isGenBuilder() : genBuilder == TRUE;
	}

	public boolean isGenSql() {
		return genSql == DEFAULT && parent != null  ? parent.isGenSql() : genSql == TRUE;
	}

	public boolean isGenMongo() {
		return genMongo == DEFAULT && parent != null  ? parent.isGenMongo() : genMongo == TRUE;
	}

	public boolean isMongoSkipNull() {
		return mongoSkipNull == DEFAULT && parent != null  ? parent.isMongoSkipNull() : mongoSkipNull == TRUE;
	}
	
	public boolean isMongoUseFieldName() {
		return mongoUseFieldName == DEFAULT && parent != null  ? parent.isMongoUseFieldName() : mongoUseFieldName == TRUE;
	}
	
}
