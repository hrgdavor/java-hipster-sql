package hr.hrg.hipster.dao.test;

import hr.hrg.hipster.dao.test.entity.*;

public class TestUpdate32 {
	
	public static void main(String[] args) {
		UserBigUpdate upd = new UserBigUpdate(null);
		upd.setName4("aa");
		UserBigMeta meta = new UserBigMeta(null,0);
		
		System.out.println(upd.isChanged(meta.name4));
		System.out.println(upd.isChanged(meta.name68));
		
	}
}
