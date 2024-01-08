package hr.hrg.hipster.processor;

import static com.squareup.javapoet.TypeSpec.*;
import static hr.hrg.javapoet.PoetUtil.*;

import com.squareup.javapoet.*;

public class GenVisitor {

	public TypeSpec.Builder gen2(EntityDef def) {
		TypeSpec.Builder builder = interfaceBuilder(def.typeVisitor);
		PUBLIC().to(builder);

		MethodSpec.Builder method = methodBuilder(PUBLIC().ABSTRACT(), "visit");
		
        int count = def.getProps().size();
        for(int i=0; i<count; i++) {
        	Property prop = def.getProps().get(i);
        	if(!prop.isTransient() && !prop.isKeepRest()) addParameter(method, prop.type, prop.fieldName);
        }        
        builder.addMethod(method.build());
        return builder;
	}
}
