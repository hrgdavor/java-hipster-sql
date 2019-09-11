package hr.hrg.hipster.dao.test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import hr.hrg.hipster.dao.IEnumGetter;
import hr.hrg.hipster.dao.jackson.DirectSerializer;
import hr.hrg.hipster.dao.jackson.IDirectSerializerReady;
import hr.hrg.hipster.sql.*;

import java.io.IOException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;

@JsonSerialize(
    using = DirectSerializer.class
)
public final class User1Immutable implements User1, IEnumGetter, IDirectSerializerReady {
  private final Long id;

  private final List<String> name;

  private final int age;

  @JsonCreator
  public User1Immutable(@JsonProperty("id") Long id, @JsonProperty("name") List<String> name,
      @JsonProperty("age") int age) {
    this.id = id;
    this.name = name;
    this.age = age;
  }

  public User1Immutable(User1 v) {
    this.id = v.getId();
    this.name = v.getName();
    this.age = v.getAge();
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public List<String> getName() {
    return name;
  }

  @Override
  public int getAge() {
    return age;
  }

  @Override
  public<T,E extends Key<T>> T getValue(E column) {
    return (T) this.getValue(column.ordinal());
  }

  @Override
  public final Object getValue(int ordinal) {
    switch (ordinal) {
    case 0: return this.id;
    case 1: return this.name;
    case 2: return this.age;
    default: throw new ArrayIndexOutOfBoundsException(ordinal);
    }
  }

  @Override
	public boolean equals(Object o) {
	  	if(o == null || !(o instanceof User1)) return false;
	  	return id.equals(((User1)o).getId());
//		return id == ((User1)o).getId();
	}
  
  public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException,
      JsonGenerationException {
    jgen.writeStartObject();

    jgen.writeFieldName("id");
    if (id == null)
    	jgen.writeNull();
    else
    	jgen.writeNumber(id);

    jgen.writeFieldName("name");
    if (name == null)
    	jgen.writeNull();
    else
    	jgen.writeObject(name);

    jgen.writeFieldName("age");
    jgen.writeNumber(age);

    jgen.writeEndObject();
  }
}
