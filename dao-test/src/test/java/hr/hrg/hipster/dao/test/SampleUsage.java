package hr.hrg.hipster.dao.test;
import javax.persistence.*;

import hr.hrg.hipster.dao.test.entity.*;

public class SampleUsage {

	public static void main(String[] args) {
		UserMeta meta = new UserMeta(null, 0);
		Id id = meta.id.getAnnotation(Id.class);
		System.out.println(id);
		Column column = meta.id.getAnnotation(Column.class);
		System.out.println(column);
		
		System.out.println(meta.age.getAnnotation(Id.class));
	}
}
