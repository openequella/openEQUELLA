# Dev Guide - Hiberate Best Practices

### Indexes
Because databases tend to do full locks on foreign key tables which causes deadlocks, we must remember to add indexes where appropriate. All @OneToOne and @ManyToOne annotations also allow for the @Index annotation on the foreign key column:
```
public class Item
{
@Id
private long id;
@OneToMany(mappedBy="item")
private List<Attachment> attachments;
}

public class Attachment
{
@Id
private long id;
@ManyToOne
@JoinColumn(name = "item_id")
@Index(name="attachmentItemIndex")
private Item item;
}
```

Please note that the ```@Index``` name must be unique across the whole DB, as Oracle uses one namespace.

Some collection types (```@CollectionOfElements``` and ```@ManyToMany```) can't be annotated with ```@Index``` however, and need special attention.

There is a call on ```HibernateMigrationHelper``` which allows indexes to be created on arbitrary tables and columns:
```
getAddIndexesRaw(String tableName, String[]... indexes);
```

```tableName``` is the raw table name each entry.  To set indexes, you provide an array which has the name of the index as the first element and all following elements are columns to be included in the index (just 1 column in the case of foreign keys).
```
helper.getAddIndexesRaw("power_search_itemdefs",
new String[]{"psid_search", "power_search_id"},
new String[]{"psid_itemdef", "itemdefs_id"}
)
```

There is also an overloaded version of ```getAddIndexesRaw()``` which takes a single index name and column as plain Strings, which is the common case.

In addition to this call you need a way of specifying these indexes for the initial schema, which is where the ```index``` parameter comes in handy, it's fairly self explanatory:
```
<extension-point id="initialSchema">
<parameter-def id="class" multiplicity="one-or-more" />
<parameter-def id="index" multiplicity="any">
<parameter-def id="table" multiplicity="one" />
<parameter-def id="name" multiplicity="one" />
<parameter-def id="column" multiplicity="one-or-more" />
</parameter-def>
</extension-point>
```

### Collections
Usually a collection can be mapped by using a foreign key, with no need for an extra association table. The following describes two scenarios for mapping a collection in this way.

Let's say you have a Parent class 'Item' and a child class 'Attachment'.

An item has a list of Attachments which have an order.

You'd like to be able to manipulate the list of attachments via the Item. E.g. removing an Attachment from the list will delete it from the database.

Use the following annotations:
```
public class Item
{
@Id
private long id;
@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
@IndexColumn(name = "attindex")
@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
@JoinColumn(name = "item_id", nullable = false)
private List<Attachment> attachments;
}

public class Attachment
{
@Id
private long id;
@ManyToOne
@JoinColumn(name = "item_id", insertable = false, updatable = false, nullable =
false)
@Index(name = "attachmentItem")
private Item item;
}
```

This will allow the following code to work as expected:
```
List<Attachment> attachments = item.getAttachments();
attachments.remove(0);
attachments.add(new Attachment());
```

Benefits of this approach:
* The java code to manipulate attachments is quite clean.
* You can still do HQL queries that involve the Item via the Attachment table. (from Attachment where item = :item)
* The Item maintains the index column for you.
* You don't need to set the Item on the Attachment, Hibernate will set it accordingly.

Some times you don't want the parent class to be the "owner" of the relationship. For example if you didn't want the Item to be responsible for deleting, adding and saving attachments, and the order of the List didn't matter.
```
public class Item
{
@Id
private long id;
@OneToMany(mappedBy="item")
private List<Attachment> attachments;
}

public class Attachment
{
@Id
private long id;
@ManyToOne
@JoinColumn(name = "item_id")
private Item item;
}

```

The previous example code will not work as expected:
```
List<Attachment> attachments = item.getAttachments();
attachments.remove(0); // does nothing
attachments.add(new Attachment()); // does nothing
// In order to add a new attachment you'll need to do the following:
Attachment attachment = new Attachment();
attachment.setItem(item);
hibernate.save(attachment);
```

In this case the List is really just an easy way of doing the query from Attachment where item = :item
