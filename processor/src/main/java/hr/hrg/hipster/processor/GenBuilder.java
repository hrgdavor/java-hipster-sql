package hr.hrg.hipster.processor;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static hr.hrg.javapoet.PoetUtil.*;

import com.squareup.javapoet.*;

public class GenBuilder {

	public GenBuilder(ClassName columnMetaBase) {
	}


	public TypeSpec.Builder gen2(EntityDef def) {
		TypeSpec.Builder cp = classBuilder(def.typeBuilder);
		cp.superclass(def.typeUpdate);
		PUBLIC().to(cp);
        
		if(def.isInterface){			
			cp.addSuperinterface(def.type);
		}else{
			cp.superclass(def.type);
		}
        
		MethodSpec.Builder b = methodBuilder(PUBLIC(), def.typeImmutable, "build");
		b.addCode("return new $T(this);\n", def.typeImmutable);
		cp.addMethod(b.build());

        int count = def.getProps().size();
        for(int i=0; i<count; i++) {
        	Property prop = def.getProps().get(i);			
			b = methodBuilder(PUBLIC(), def.typeBuilder, prop.fieldName);
			addParameter(b, prop.type, "v");
			b.addCode(prop.setterName+"(v); return this;\n");
			cp.addMethod(b.build());
        }
        
        genConstructors(def, cp);
        
		
        if(def.genOptions.isGenJson()) GenImmutable.addDirectSerializer(def,cp);
        
        return cp;
	}

		
	public static void genConstructors(EntityDef def, TypeSpec.Builder cp){
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
