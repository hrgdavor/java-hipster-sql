package hr.hrg.hipster.sql;

import org.joda.time.*;

import hr.hrg.hipster.sql.setter.*;

public class PreparedSetterSource extends HandlerSource<IPreparedSetter<?>>{


    public PreparedSetterSource() {
		registerBoth(new BooleanSetter(), Boolean.class);
		registerBoth(new IntegerSetter(), Integer.class);
		registerBoth(new LongSetter(), Long.class);
		registerBoth(new FloatSetter(), Float.class);
		registerBoth(new DoubleSetter(), Double.class);
		registerBoth(new ShortSetter(), Short.class);
		registerBoth(new ByteSetter(), Byte.class);

		registerBoth(new BooleanSetter(), boolean.class);
		registerBoth(new IntegerSetter(), int.class);
		registerBoth(new LongSetter(), long.class);
		registerBoth(new FloatSetter(), float.class);
		registerBoth(new DoubleSetter(), double.class);
		registerBoth(new ShortSetter(), short.class);
		registerBoth(new ByteSetter(), byte.class);

		registerBoth(new StringSetter(), String.class);

		registerBoth(new DateSetter(), java.util.Date.class);
		registerBoth(new SqlDateSetter(), java.sql.Date.class);
		
		if(HipsterSqlUtil.isYodaPresent()) {
			registerBoth(new DateTimeSetter(), DateTime.class);
			registerBoth(new LocalTimeSetter(), LocalTime.class);
			registerBoth(new LocalDateSetter(), LocalDate.class);
		}
	}

}
