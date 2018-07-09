# Authentication and security

Early on in openEQUELLA request processing a couple of servlet filters run to determine two things:

* Which institution the request is for (if any). `com.tle.web.institution.InstitutionFilter`
* If the request contains information about the current user. `com.tle.web.core.filter.TleSessionFilter`

After those two filters have run the current request thread will contain two thread locals which give you access to this information:

```java
UserState user = CurrentUser.getUserState();
Institution inst = CurrentInstitution.get();
```

**NOTE:** The use of thread locals is an anti-pattern because it means that your code 
must be run on a thread which contains those thread locals. 
It is best to just use these in the very top level code, such as the REST API resource and just pass 
`UserState` and `Institution` around explicitly in any service/DAO code.

---
The `UserState` holds some important information about the user, such as:

* Is the user actually logged in? - `user.isGuest()`
* Is the user the TLE_ADMINISTRATOR? - `user.isSystem()`
* What is the user's id/username/name/email address? - `user.getUserBean()`


## Privileges

Rather than using `isGuest()` and `isSystem()` for access control, you will want to instead check if the current user has
a given privilege, such as `DISCOVER_ITEM` or `CREATE_COLLECTION`.

Individual privileges are assigned to a particular entity such as an individual Schema or Collection (`ItemDefinition`) object 
or virtual entity such as "All Collections" or "All Items with the LIVE status".

The various different places at which a privilege can be assigned form a tree structure, with the `Institution` level being the root of the tree and the actual entity itself being at leaf.


The old architecture favoured aspect oriented programming (e.g. annotations that do magic interception) to check privileges. 

New code should however explicitly make calls to check privileges, depending on if you're using Java or Scala there are different ways to achieve this.

Java code should use `TLEAclManager` to check privileges. It is responsible for determining, given a domain object, which parts of the tree need to be checked for the privilege and querying just those places. For example if given a Schema object it will check - Schema, All Schemas and Institution.
Some of the useful methods in `TLEAclManager`:

```java
    /* For filtering out a list of objects for which you don't have ALL the privileges for */
    <T> Collection<T> filterNonGrantedObjects(Collection<String> privileges, Collection<T> domainObjs)

    /* Checking a single privilege on a single domain object */
	<T> boolean checkPrivilege(String privilege, T domainObj);

    /* When you need to know exactly which privileges are available for a list of objects */
	<T> Map<T, Map<String, Boolean>> getPrivilegesForObjects(Collection<String> privileges, Collection<T> domainObjs);

    /* For checking privileges which don't apply to a particular object, e.g. CREATE_COLLECTION */
	Set<String> filterNonGrantedPrivileges(Collection<String> privileges, boolean includePossibleOwnerAcls);
```

Scala code can use the `AclChecks` object to check "top level" privileges in the [DB](scaladb.md) monad, 
this will be expanded further to check privileges against particular domain objects.

```scala
def ensureOnePriv[A](privs: String*)(db: DB[A]): DB[A]

def filterNonGrantedPrivileges(privileges: Iterable[String], includePossibleOwnerAcls: Boolean): DB[Set[String]]
```

TODO - examples
