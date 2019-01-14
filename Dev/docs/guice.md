# Guice dependency injection

"Guice (pronounced 'juice') is a lightweight dependency injection framework for Java 6 and above, brought to you by Google."

[https://github.com/google/guice](https://github.com/google/guice)

Rather that explain Guice itself, this document will describe the openEQUELLA extensions and 
give a simple tutorial on usage.

* Injector per JPF Plugin
* Classpath scanning for registering instances/classes
* Helper classes for looking up JPF extensions via Injector 
* Binding config options from config properties files
* DB transactions and security

# Injector per JPF Plugin

Each JPF plugin can have it's own Guice `Injector` which in turn can look up bindings from 
dependent JPF plugins. In order to have an `Injector` created for your plugin you must put 
an extension into your `plugin-jpf.xml`:

```xml  
<plugin id="mypluginid" version="1">
  <requires>
    <import plugin-id="com.tle.core.guice" />
    <import plugin-id="some.other.plugin" />
    <!-- other imports -->
  </requires>
  
  <extension plugin-id="com.tle.core.guice" point-id="module" id="guiceModules">
     <parameter id="class" value="myplugin.MyGuiceModule" />
     <!-- Add as many Guice modules as you'd like -->
  </extension
</plugin>
```

The class `myplugin.MyGuiceModule` should extend `com.google.inject.Module`. 
You don't necessarily need to create a `Module` however, as it's possible that you can 
use the **Classpath scanning** feature to create your bindings. There are also a set of 
openEQUELLA specific Guice Modules which are useful to add depending on what your Plugin will do.     

Also note that for any `@Inject` lookups in your plugin which can't be found, dependent plugins 
(`some.other.plugin` in the example) will be used to lookup the binding. 

Generally you won't need to interact directly with the `Injector` itself as your 
classes will normally be instantiated by Guice itself and @Inject dependent instances, 
however if you do need that level of access, `PluginService` gives you programmatic 
access to looking up arbitrary Plugin's `Injector`s.


# Classpath scanning

A standard use case for Guice is to create a Service interface which has it's implementation bound
using Guice. For example:

```java
public interface MyService
{
	String doSomething();
}

public class MyServiceImpl implements MyService
{
	public String doSomething()
	{
		return "I did something";
	} 
}

public class MyClient
{
	@Inject
	private MyService myService;
	
	public void run()
	{
		System.out.prinlnt(myService.doSomething());
	}
}

public class MyClientModule extends AbstractModule
{
	protected void configure()
  {
		bind(MyService.class).to(MyServiceImpl.class);
	}
}
```  

This is a good pattern to follow but 99% of the time you will want to bind `MyService` to `MyServiceImpl`. 
Probably the only time you would want to not do that is if you were creating a Unit test and wanted to mock the service to something else. So to save you the effort of doing the `bind` line, openEQUELLA has implemented scanning for the
`@com.tle.core.guice.Bind` annotation which can handle the very common case for you:

```java
@Bind(MyService.class)
public class MyServiceImpl implements MyService
{
	...
}
```

So in this case you no longer need the `MyClientModule` at all as it would be empty.

If you leave out the `(MyService.class)` of the @Bind annotation, the binding will be for just the actual class itself. So the equivalent of `@Bind(MyServiceImpl.class)`.

# Plugin Extension Tracker

If a JPF extension point has an associated `interface`/`base class` which the extensions must implement,
the easiest way to instantiate/query those extensions is by using the helper class `com.tle.core.plugins.PluginTracker`.

The `PluginTracker` is responsible for keeping track of all connected extensions and gives you methods to:

* Enumerate extensions - `List<Extension> getExtensions()`
* Instantiate and cache a particular extension "bean" - `T getBeanByExtension(Extension extension)`

The `PluginTracker` can be instantied directly with `new` but the best way to use it is to bind an instance with a guice module. Given the following extension point declaration and interface class:

```xml
<plugin id="my.welcome.plugin" version="1">
  <extension-point id="myextensionpoint">
    <parameter-def id="bean" multiplicity="one" type="string" />
  </extension-point>
</plugin>
```

```java
public interface MyExtensionPoint
{
	String welcomeText();
}
```

You can create a service which can lookup the extensions by `@Inject`ing a `PluginTracker` instance:

```java
@Bind
public class MyWelcomeService
{
	@Inject 
	private PluginTracker<MyExtensionPoint> extensionTracker;
	
	public String getWelcomeMessage()
	{
		List<String> welcomes = extensionTracker.getBeanList().stream()
		    .map(w -> w.welcomeText()).collect(Collectors.toList());
		return String.join(", ", welcomes);
	}
} 
```

In order to bind the `PluginTracker` you need to create and register a 
guice module which extends `PluginTrackerModule`:

```java
public class ExtensionTrackers extends PluginTrackerModule
{
	@Override
	protected String getPluginId()
	{
		return "my.welcome.plugin";
	}

	@Override
	protected void configure()
	{
		bindTracker(MyExtensionPoint.class, "myextensionpoint", "bean");
	}	
}
```

In order to implement the extension you can simply create your implementing class(es):

```java
package hello.world;

@Bind 
public class HelloWorldGuice implements MyExtensionPoint
{
	@Inject
	private SomeService service;
	
	String welcomeText()
	{
		return "Hello World";
	}
}
```
```java
package willkommen;

public class Willkommen implements MyExtensionPoint
{
	String welcomeText()
	{
		return "Willkommen";
	}	
}
```

And ensure your `plugin-jpf.xml` has an entry for the extension:

```xml 
<plugin id="myhelloworld" version="1">
  <requires>
    <import plugin-id="com.tle.core.guice" />
    <import plugin-id="my.welcome.plugin" />
    <!-- other imports -->
  </requires>
  
  <extension plugin-id="com.tle.core.guice" point-id="module" id="guiceModules"/>
  
  <extension plugin-id="my.welcome.plugin" point-id="myextensionpoint" id="helloworld">
    <parameter id="bean" value="bean:hello.world.HelloWorldGuice" />
  </extension>

  <extension plugin-id="my.welcome.plugin" point-id="myextensionpoint" id="willkommen">
    <parameter id="bean" value="willkommen.Willkommen" />
  </extension>
</plugin>
``` 

Please note that the "helloworld" extension "bean" parameter has it's value 
prefixed with "`bean:`" which means that the extension instance should be 
retrieved from the Guice Injector for the plugin. If you don't include "`bean:`", 
an instance will be created by Reflection using the given classes no arg constructor.

**NOTE**

In general the JPF Guice integration should be for legacy code or for genuine extensions which Third Parties 
might want to extend. If the extension interface should only be exposed to the core then it is much better to
generate the list of extensions in code as it's more type safe. For example it would be better
to create a class which tracked the `MyExtensionPoint`:

```java
@Bind
@Singleton
public class MyExtensions
{
    @Inject 
    private HelloWorldGuice helloWorld;
    
    private Willkommen willkommen = new WillKommen();
    
    public List<MyExtensionPoint> getGreeters()
    {
    	return Collections.asList(helloWorld, willkommen);
    }
}
```

# Binding server config properties

Server level configuration properties can be retrieved by guice created instances by extending
some pre-defined `Module` classes. In order to retrieve settings from the `"mandatory-config.properties"` 
you can extend `com.tle.core.config.guice.MandatoryConfigModule`, for `"optional-config.properties"` use
`com.tle.core.config.guice.OptionalConfigModule`. These classes have various methods for binding 
primitive values to named instances, such as `bindInt()`, `bindLong()` and `bindBoolean()`.

```java
public class MyServerConfig extends OptionalConfigModule
{
	public void configure()
	{
		bindInt("my.number.property", -1);
	}
}

@Bind
@Singleton
public class MyGuiceService
{
	@Named("my.number.property")
	private int configuredNumber;
}
```

learningedge-config/optional-config.properties:
```properties 
my.number.property = 100
```

# AOP based transactions and security

Guice can handle AOP by scanning for annotations on methods/classes and openEQUELLA uses
those facilities for DB transactions and security.

For DB transactions, openEQUELLA supports the Spring `@Transactional` annotation which is required
in order to make writes persist to the DB as by default the DB access is read-only. To include 
support for this you must include `com.tle.core.hibernate.guice.TransactionModule`.

To secure your services, you can add security annotations such as `@SecureOnCall` and `@SecureReturn` 
which can be used to prevent access or filter the results of a method. To use these you must include 
`com.tle.core.security.guice.SecurityModule`. There are quite a few security annotations (~8) and 
their usage is out of scope for this particular document.

The common case for Java services is that you need support for both DB transactions and Security annotations,
so there is a handy module which installs both modules together, `com.tle.core.services.guice.ServicesModule`.


   
