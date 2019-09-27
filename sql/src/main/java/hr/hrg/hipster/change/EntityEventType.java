package hr.hrg.hipster.change;

import java.util.*;

public enum EntityEventType{
	BEFORE_ADD,	   AFTER_ADD,
	BEFORE_CHANGE, AFTER_CHANGE,
	BEFORE_DELETE, AFTER_DELETE;
	
	public static final EntityEventType[] ALL_AFTER_ARRAY = {AFTER_ADD, AFTER_CHANGE, AFTER_DELETE};

	public static final EnumSet<EntityEventType> ALL_AFTER = EnumSet.of(AFTER_ADD, AFTER_CHANGE, AFTER_DELETE);
}