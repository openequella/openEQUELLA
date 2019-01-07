# Guice dependency injection

"Guice (pronounced 'juice') is a lightweight dependency injection framework for Java 6 and above, brought to you by Google."

[https://github.com/google/guice](https://github.com/google/guice)

Rather that explain Guice itself, this document will describe the openEQUELLA extensions and 
give a simple tutorial on usage.

* Injector per JPF Plugin
* Classpath scanning for registering instances/classes
* Helper classes for looking up JPF extensions via Injector 
* Binding config options from config properties files
* Standard openEQUELLA Guice Modules

# Injector per JPF Plugin

Each JPF plugin can have it's own Guice `Injector` which in turn can look up bindings from 
dependant JPF plugins. In order to have an `Injector` created for your plugin you must put 
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

Also note that for any `@Inject` lookups in your plugin which can't be found, dependant plugins 
(`some.other.plugin` in the example) will be used to lookup the binding. 

Generally you won't need to interact directly with the `Injector` itself as your 
classes will normally be instantiated by Guice itself and @Inject dependant instances, 
however if you do need that level of access, `PluginService` gives you programatic 
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
<plugin id="my.extendable.plugin" version="1">
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

