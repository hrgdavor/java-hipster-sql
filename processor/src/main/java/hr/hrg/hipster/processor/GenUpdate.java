package hr.hrg.hipster.processor;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static hr.hrg.hipster.processor.HipsterProcessorUtil.*;
import static hr.hrg.javapoet.PoetUtil.*;

import com.squareup.javapoet.*;

import hr.hrg.hipster.entity.*;
import hr.hrg.hipster.sql.*;

public class GenUpdate {

	private ClassName columnMetaBase;

	public GenUpdate(ClassName columnMetaBase) {
		this.columnMetaBase = columnMetaBase;
	}

	public TypeSpec.Builder gen2(EntityDef def) {

		TypeSpec.Builder cp = classBuilder(def.typeUpdate);
		PUBLIC().to(cp);
        
		if(def.genOptions.isGenBuilder()){
			cp.superclass(def.typeBuilder);			
		}else{
			cp.addSuperinterface(def.type);
			GenBuilder.addInterfaces(def, cp, columnMetaBase);
		}
		
		int propCount = def.props.size();
		for(int i=0; i*64<propCount; i+=1) {
			addField(cp,PROTECTED(), long.class, "_changeSet"+i);
		}

        int count = def.getProps().size();
        for(int i=0; i<count; i++) {
        	Property prop = def.getProps().get(i);
        	
        	if(!def.genOptions.isGenBuilder()){
            	if(prop.initial != null) {
    				String typeStr = prop.type.toString();
            		if(typeStr.equals("java.lang.String"))
            			addField(cp, PROTECTED(), prop.type, prop.name, "$S", prop.initial);
            		else
            			addField(cp, PROTECTED(), prop.type, prop.name, "$L", prop.initial);
            	}else {        		
            		addField(cp, PROTECTED(), prop.type, prop.name);
            	}
        		MethodSpec.Builder g = methodBuilder(PUBLIC(), prop.type, prop.getterName);
				// it would be done there
				GenImmutable.copyAnnotations(g, prop);				
				g.addCode("return "+prop.fieldName+";\n");
				cp.addMethod(g.build());
			}

			MethodSpec.Builder bm = methodBuilder(PUBLIC(), def.typeUpdate, prop.setterName);
			int changeSet = i/64;
			if(def.genOptions.isInspectChange()) {
				bm.addCode("if(this.$L == $L) return this;\n", prop.name,prop.name);					
				if(!prop.isPrimitive()) {
					bm.addCode("if(this.$L !=null && !this.$L.equals($L)) return this;\n", prop.name,prop.name, prop.name);
				}
			}
			bm.addCode("this._changeSet"+changeSet+" |= "+(1L<<i)+"L;\n");
			addSetterParameter(bm, prop.type, prop.name, null);
			bm.addCode("return this;\n");
			cp.addMethod(bm.build());
        }
        
        MethodSpec.Builder setValue = null;
        if(def.genOptions.isGenBuilder()){
			setValue = methodBuilder(PUBLIC(), void.class, "setValue" );
	        setValue.addAnnotation(Override.class);
	        addParameter(setValue,int.class, "_ordinal");
	        addParameter(setValue,Object.class, "value");
	        setValue.addCode("super.setValue(_ordinal, value);\n");
	        genConstrucotrsExt(def, cp);
        }else{
        	setValue = GenBuilder.genEnumSetter(def, cp,columnMetaBase);
        	GenBuilder.genConstructors(def, cp);
            GenImmutable.addEnumGetter(def, cp,columnMetaBase);
            GenImmutable.addEquals(def, cp);
            if(def.genOptions.isGenJson()) GenImmutable.addDirectSerializer(def,cp);
            
        }
        makeOrdinalExpr(propCount, setValue, "this._changeSet$L |= (1L<<_ordinal);\n");
        cp.addMethod(setValue.build());


        // *********************  IUpdatatable
        
        TypeVariableName typeT = TypeVariableName.get("T");
		ParameterizedTypeName typeKey = parametrized(Key.class, typeT);
		
        addMethod(cp, PUBLIC(), boolean.class, "isEmpty", method -> {
        	method.addAnnotation(Override.class);
        	method.addCode("return ");
    		for(int i=0; i*64<propCount; i+=1) {
    			if(i>0) method.addCode(" && ");
    			method.addCode("_changeSet"+i+" == 0");
    		}
        	method.addCode(";\n");
        });
        
        addMethod(cp, PUBLIC(), boolean.class, "isChanged", method -> {
        	method.addAnnotation(Override.class);
        	method.addTypeVariable(typeT);
        	addParameter(method, typeKey, "column");
           	method.addCode("return isChanged(column.ordinal());\n");
        });
        
        addMethod(cp, PUBLIC(), boolean.class, "isChanged", method -> {
			method.addAnnotation(Override.class);
			addParameter(method, int.class, "_ordinal");
	        makeOrdinalExpr(propCount, method, "return (_changeSet$L & (1L << _ordinal)) != 0;\n");
        });


        addMethod(cp, PUBLIC(), void.class, "setChanged", method -> {
			method.addAnnotation(Override.class);
        	method.addTypeVariable(typeT);
			addParameter(method, typeKey, "column");
			addParameter(method, boolean.class, "changed");
			method.addCode("setChanged(column.ordinal(), changed);\n");
		});
        
        addMethod(cp, PUBLIC(), void.class, "setChanged", method -> {
			method.addAnnotation(Override.class);
			addParameter(method, int.class, "_ordinal");
			addParameter(method, boolean.class, "changed");
			method.addCode("if(changed) {\n");
	        makeOrdinalExpr(propCount, method, "this._changeSet$L |= (1L<<_ordinal);\n");
			method.addCode("}else{\n");
			makeOrdinalExpr(propCount, method, "this._changeSet$L &= ~(1L<<_ordinal);\n");
			method.addCode("}\n");
		});

        addMethod(cp, PUBLIC(), void.class, "setChanged", method -> {
			method.addAnnotation(Override.class);
			addParameter(method, boolean.class, "changed");
			method.addCode("if(changed){\n");
    		for(int i=0; i*64<propCount; i+=1) {
    			method.addCode("\t_changeSet"+i+" = ");
    			if(((i+1)*64)>=propCount) {    				
    				method.addCode(""+((1<<propCount)-1)+";\n");
    			}else {
    				method.addCode("-1;\n");    				
    			}
    		}
    		method.addCode("}else{\n");
    		for(int i=0; i*64<propCount; i+=1) {
    			method.addCode("\t_changeSet"+i+" = 0;\n");
    		}
    		method.addCode("}\n");
			
		});
        
        return cp;
	}

	public void makeOrdinalExpr(int propCount, MethodSpec.Builder setValue, String expr) {
		if(propCount <64) {
        	setValue.addCode(expr,0);        	
        }else {
        	boolean first = true;
        	for(int i=8; i>=0; i--) {
        		if(i*64>propCount) continue;
        		if(first) {
        			setValue.addCode("if(_ordinal >="+(i*64)+"){\n");        			
        		}else {
        			setValue.addCode("}else");
        			if(i>0) setValue.addCode(" if(_ordinal >="+(i*64)+")");
        			setValue.addCode("{\n");        			        			
        		}
        		setValue.addCode("\t");
        		setValue.addCode(expr,i);
        		
        		first = false;
        	}
        	setValue.addCode("}\n");
        }
	}
	
	
	
	public static void genConstrucotrsExt(EntityDef def, TypeSpec.Builder cp){
        MethodSpec.Builder constr = constructorBuilder(PUBLIC());
        constr.addCode("super();\n");
        cp.addMethod(constr.build());

        constr = constructorBuilder(PUBLIC());
        addParameter(constr, def.type, "v");
        constr.addCode("super(v);");
        cp.addMethod(constr.build());

        constr = constructorBuilder(PUBLIC());

        constr.addCode("super(");
        
        int count = def.getProps().size();
        for(int i=0; i<count; i++) {
        	Property property = def.getProps().get(i);
        	addParameter(constr, property.type, property.name);        	

            constr.addCode(""+property.name+(i == count-1 ? "":","));
        }
        
        constr.addCode(");\n");
        cp.addMethod(constr.build());
	}
	
}
