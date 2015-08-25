zorm-2007.06.21-dev (development version)

http://code.google.com/p/zorm

ZORM is a Java object relational mapping framework (MIT license).

It provides an easy way to define a simple object oriented data access layer to a 
relational database. 

Features:
	* Simple to use
    * Minimal ussage of reflection for performance reasons
    * Seamless configuration inside Java code
    * Persistent objects caching
    * Partially loading of persistent objects (fetch a subset of fields from db)
	* Flexible queries (access to all SQL select functionality)
	* Static checked (compile time) query capabilities

This is a development version. Some parts that are essential for production ussage, 
like support for SQL types, update and delete custom queries, are missing at the moment.
Also, the documention is pretty scarce.
Until this changes, you can find more info from the Javadoc documentation (docs/api/index.html),
or from the test code in src/test directory.

Dependencies:
	log4j
	zutil  - http://code.google.com/p/zutil
	testng - for tests
    	
A build script is on the TODO list.

===============================================================================
Here is a simple example of ussage:

-------------------------------------------------------------------------------
Defining a mapping for a user table having 3 fields (id, name, rating):

public final class User extends ZPersistent {

	public final static ZPersistentMeta META = new ZPersistentMeta(User.class,
			"user", "u");

	public final static ZStringField ID = new ZStringField("id");

	public final static ZStringField NAME = new ZStringField("name");

	public final static ZIntField RATING = new ZStringField("rating");

	static { // fields initialization
		META.setFields(ID, NAME, RATING);
	}

	@Override
	public ZPersistentMeta getMeta() {
		return META;
	}

	public void setId(String id) {
		ID.setValue(this, id);
	}

	public String getName() {
		return NAME.getValue(this);
	}

	public void setName(String value) {
		NAME.setValue(this, value);
	}

	public int getRating() {
		return RATING.getValue(this);
	}

	public void setRating(int value) {
		RATING.setValue(this, value);
	}
}

-------------------------------------------------------------------------------
To retrieve a user from the database:

ZSession session = ZManager.getNewSession();
// will issue to db: select id, name, rating from user where id = 'alice'
User user = (User) session.get(User.META, "alice");
System.out.println("id = " + user.getId() + " name = " + user.getName() + " rating = " + user.getRating());
session.close();


-------------------------------------------------------------------------------
To create a new user:

User user = session.getNew(User.class);
user.setId("john");
user.setName("John Doe");
user.setRating(10);
// will issue to db: INSERT INTO item (id, name, rating) VALUES ('john', 'John Doe', '10')
user.save();

-------------------------------------------------------------------------------
To get all the users with rating bigger then 5, sorted by name
Object[] users = getSession().getSelectQuery()
		.select(User.META)
		.from(User.META)
		.where(User.RATING, Z.EQUALS, '5');
		.sortBy(User.NAME)
		.executeUniqueSelect();

for (Object ob : user) {
	User user = (Object) ob;
	System.out.println("id = " + user.getId() + " name = " + user.getName() + " rating = " + user.getRating());
}
-------------------------------------------------------------------------------
