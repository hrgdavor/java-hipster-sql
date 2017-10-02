package hr.hrg.hipster.sql;

import org.joda.time.*;

import hr.hrg.hipster.sql.getter.*;

public class ResultGetterSource extends HandlerSource<IResultGetter<?>>{

    public ResultGetterSource() {
		registerBoth(new BooleanGetter(), Boolean.class);
		registerBoth(new IntegerGetter(), Integer.class);
		registerBoth(new LongGetter(), Long.class);
		registerBoth(new FloatGetter(), Float.class);
		registerBoth(new DoubleGetter(), Double.class);
		registerBoth(new ShortGetter(), Short.class);
		registerBoth(new ByteGetter(), Byte.class);

		registerBoth(new BooleanGetter(), boolean.class);
		registerBoth(new IntegerGetter(), int.class);
		registerBoth(new LongGetter(), long.class);
		registerBoth(new FloatGetter(), float.class);
		registerBoth(new DoubleGetter(), double.class);
		registerBoth(new ShortGetter(), short.class);
		registerBoth(new ByteGetter(), byte.class);
		
		registerBoth(new StringGetter(), String.class);

		registerBoth(new DateGetter(), java.util.Date.class);
		registerBoth(new SqlDateGetter(), java.sql.Date.class);
		
		if(HipsterSqlUtil.isYodaPresent()) {
			registerBoth(new DateTimeGetter(), DateTime.class);
			registerBoth(new LocalTimeGetter(), LocalTime.class);
			registerBoth(new LocalDateGetter(), LocalDate.class);
		}
	}

	
}
