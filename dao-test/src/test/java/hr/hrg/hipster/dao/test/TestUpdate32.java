package hr.hrg.hipster.dao.test;

public class TestUpdate32 {
	
	public static void main(String[] args) {
		UserBigUpdate upd = new UserBigUpdate();
		upd.name4("aa");
		UserBigMeta meta = new UserBigMeta(null,0);
		
		System.out.println(upd.isChanged(meta.name4));
		System.out.println(upd.isChanged(meta.name68));
		
	}
}
