package hr.hrg.hipster.dao.test;

import java.util.*;

import hr.hrg.hipster.dao.test.entity.*;

public class TestBuilder {

	public static void main(String[] args) {
		UserBuilder buser = new UserBuilder().age(45);
		User user = new UserBuilder().age(45).build();
	}
}
