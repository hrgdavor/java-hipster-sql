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
String queryStr = "select * from users where name LIKE'"+name+"%'";

// in our metohds query parts are passed as arguments and prepared statments are used in the background 
// the only difference from the unsafe query string above is that "+" plus operator is replaced by "," comma
q("select * from users where name LIKE '",name,"%'");

// Sure, using a simple prepared statement like this is not so bad, but it gets complicated quickly
pStatement = conn.prepareStatement("select * from users where name LIKE ?");
pStatement.setString(1, name+"%");

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

```java



```


## License

See the [LICENSE](LICENSE.md) file for license rights and limitations (MIT).
