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

public MyClientModule extends AbstractModule
{
	
}
```  
