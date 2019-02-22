# REST API endpoints

The new architecture emphasizes communicating to the backend server via REST endpoints using JSON
representations.

RestEasy 3.5.0 configured using [JAX-RS](https://docs.oracle.com/javaee/6/tutorial/doc/giepu.html) annotations and Jackson for JSON de/serialization.
On top of that Swagger annotations are used for documentation.

- [Add an endpoint](#adding)
- [Important annotations](#important_annotations)
- [JSON de/serialization](#json)
- [Creating URIs](#uri)

### <a href="#adding"></a>Adding a new API resource

Create your API resource class:

Java:

```java
@Path("testrest/")
@Api("Test rest")
// @Bind - if you need to wire in Guice
public class TestResource {

    @GET
    @Path("")
    @Produces("text/plain")
    public String myInstUrl(@Context UriInfo info)
    {
        return info.getBaseUri().toString();
    }
}
```

Scala:

```scala
@Path("testrest/")
@Api("Test rest")
class TestResource {

    @GET
    @Path("")
    @Produces("text/plain")
    def myInstUrl(@Context info: UriInfo): String =
        info.getBaseUri.toString

}
```

Edit `RestEasyServlet.java` to add the class to scan for annotations and provide an instance of the class.

```java
registry.addSingletonResource(new TestResource());
// If you need to wire in services with Guice use this:
// registry.addResourceFactory(new BeanLocatorResource(TestResource.class, coreLocator));
classes.add(TestResource.class);
```

Voila! You should now have a new REST API accessible from `<insturl>/api/testrest` and it should show up on the Swagger docs.

## Important annotations

| Annotation                             |                                                                                                                                                                                              |
| -------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `@javax.ws.rs.Path`                    | Your class needs to specify the base URI from which all methods are relative to. This itself is relative to the `<insturl>/api/`. Individual methods are relative to the class-level `@Path` |
| `@io.swagger.annotations.Api`          | This is the name that will show on the swagger api docs page.                                                                                                                                |
| `@io.swagger.annotations.ApiOperation` | This is the description that will show for a method on the api docs page.                                                                                                                    |

## <a href="#json"></a>JSON de/serialization with Jackson

Consider the following Java/Scala classes:

```java
public class JsonClassJava {

    private int field1;
    private String field2;
    private List<String> strings;

    public int getField1()
    {
        return field1;
    }

    public void setField1(int field1)
    {
        this.field1 = field1;
    }

    public String getField2()
    {
        return field2;
    }

    public void setField2(String field2)
    {
        this.field2 = field2;
    }

    public List<String> getStrings()
    {
        return strings;
    }

    public void setStrings(List<String> strings)
    {
        this.strings = strings;
    }
}
```

```scala
case class JsonClassScala(field1: Int, field2: Option[String], strings: Iterable[String])
```

Both can be translated into JSON such as:

```json
{
  "field1": 123,
  "field2": "My value",
  "strings": ["string1", "string2"]
}
```

They can be automatically converted to/from JSON using Jackson (using `jackson-module-scala` for Scala)

E.G.

```java
    @PUT
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonClassJava jsonInOut(JsonClassJava in)
    {
        in.setField2("changed");
        return in;
    }
```

```scala
    @PUT
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    def jsonInOut(in: JsonClassScala): JsonClassScala =
    {
        in.copy(field2 = in.field2.orElse(Some("changed")))
    }
```

**NOTE:** Nulls are disallowed in the Scala deserializer, so you must use `Option` instead.

## <a href="#uri"></a>Creating URIs

REST endpoints should provide URI links to relevant resources. The [URIInfo](https://docs.oracle.com/javaee/6/api/javax/ws/rs/core/UriInfo.html)
context object should be used to generate the base URIs to build on top of:

```java


@Path("testrest/")
@Api("Test rest")
public class TestResource {
    @Context UriInfo uriInfo;

    @GET
    public URI generateUri() {
        return uriInfo.getBaseUriBuilder().path(getClass()).path(getClass(), "methodName").build(); // returns <insturl>/api/testrest/something/
    }

    @GET
    @Path("something/")
    public String methodName()
    {
        return "something";
    }
}
```
