#Introduction

Utility for watching working with JDBC. When ORM feels too heavy, and pure JDBC annoying.


#Usage

Add maven dependency or download from [maven central](http://repo1.maven.org/maven2/hr/hrg/java-hipster-sql/)

```
<dependency>
	<groupId>hr.hrg</groupId>
	<artifactId>java-hipster-sql</artifactId>
	<version>0.1.0</version>
</dependency>
```

# SQL injection and prepared statements
Maybe you just want to write the query in the code in a way that is readable and easy to maintain.

```java
// I prefer to have, the variables that are part of the query, inline with sql code, 
// but something like this could easily become sql injection problem if name variable comes from user input
// ** Mainstream style
queryStr = "select * from users where name LIKE '"+name+"' and height > "+height+" order by name";

// in our metohds query parts are passed as arguments and prepared statments are used in the background 
// the only difference from the unsafe query string above is that "+" plus operator is replaced by "," comma
// ** Hipster style
query = new Query("select * from users where name LIKE ",name," and height > ",height," order by name");

// Sure, using a simple prepared statement for something like this is not so bad, but it gets complicated quickly
// ** Mainstream style
pStatement = conn.prepareStatement("select * from users where name LIKE ?  and height > ? order by name");
pStatement.setString(1, name);
pStatement.setInt(2, height);

// but if you like the prepared statement syntax, you can also use varargs with PreparedQuery
// ** Hipster style
pQeury = new PreparedQuery("select * from users where name LIKE ?  and height > ? order by name", name, height);


```

# QueryUtil
Choose your style how queries will look in code with static methods from [QueryUtil.java](src/main/java/hr/hrg/hipstersql/QueryUtil.java)

```java

import static hr.hrg.hipstersql.QueryUtil.*; // "import static" utility methods from QueryUtil
import hr.hrg.hipstersql.Query;

// creating new query is not very complicated
query =    new Query("select * from users where id=",id);
somemethod(new Query("select * from users where id=",id));

// arguably can maybe make code a bit more readable, by using QueryUtil.q static method 
// (depends on personal style/preference)
query =    q("select * from users where id=",id);
somemethod(q("select * from users where id=",id));


```

# HipsterSql utility methods
Some utility methods are not suitable to be static as the result might differ slightly based
on database you are connected to (slight syntax changes are possible). Also you might want to
override some methods in case of specific needs (a feature not covered or even a bug not fixed yet).

For both examples we will prepare a map with data. We create a map for the two examples, 
but you might have it already by reading data from somewhere

```java
Map<Object,Object> data = new HashMap<>();
data.put("name", name);
data.put("gender", gender);
data.put("age", gender);
// to pass a function call, just pass a new query object instead of the value
data.put("password", q("PASSWORD(",password,")") );
```

## HipsterSql.buildInsert and buildInsertVar 
buildInsert helps with generating insert queries

```java
hip.buildInsert("user", data);

// varargs version if you want to inline the parameters and not create a map with data
hip.buildInsertVar("user", 
		"name", name,
		"gender", gender,
		"age", gender,
		// to pass a function call, just pass a new query object instead of the value
		"password", q("PASSWORD(",password,")")
	);
// resulting prepared statement 
// INSERT INTO user(name,gender,age,password VALUES(?,?,?,PASSWORD(?))
```

## HipsterSql.buildUpdate and buildUpdateVar
buildUpdate helps with generating update queries very similar to buildInsert.
The second parameter is filter to limit updates scope

```java
// we can reuse the map from last example
hip.buildUpdate("user", q("id=",id) ,data);

// varargs version if you want to inline the parameters and not create a map with data
hip.buildUpdateVar("user", q("id=",id),  
		"name", name,
		"gender", gender,
		"age", gender,
		// to pass a function call, just pass a new query object instead of the value
		"password", q("PASSWORD(",password,")")
	);
// resulting prepared statement 
// UPDATE user SET name=?,gender=?,age=?,password=PASSWORD(?) WHERE id = ?
```

## HipsterSql.buildFilter
buildFilter can be useful as basis for you to allow users to supply filter for queries in a controllable fashion

```java
// with only 2 parameters "=" operator is assumed 
query = hip.buildFilter("id",id);
// resulting prepared statement
// id = ?

// when handling null, this function becomes even more useful as it changes "id = null" to "id IS NULL"
query = hip.buildFilter("id",null);
// resulting prepared statement
// id IS NULL
prepare = hip.prepare(query);

// similar behavior is also with "!=" and "<>" operator and null value
query = hip.buildFilter("id","!=",null);
assertEquals(hip.prepare(query).getQueryString(), "id IS NOT NULL");
query = hip.buildFilter("id","<>",null);
// both resulting in same prepared statement
// id IS NOT NULL
```

## License

See the [LICENSE](LICENSE.md) file for license rights and limitations (MIT).
