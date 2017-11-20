package com.tle.web.sections.equella.js;

import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.java.plugin.Plugin;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginRegistry.RegistryChangeListener;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.util.Types;
import com.tle.common.filters.Filter;
import com.tle.core.guice.PluginTrackerModule.TrackerProvider;
import com.tle.core.plugins.AbstractPluginService.TLEPluginLocation;
import com.tle.core.plugins.PluginBeanLocator;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.sections.convert.Conversion;
import com.tle.web.sections.convert.SectionsConverter;

public class ObjectExpressionDeserialiserTest extends TestCase
{
	public void testDeserialiser() throws Exception
	{
		// Not working on build server for some reason
		// setupBullshit();
		//
		// ObjectMapper mapper = new ObjectMapper();
		// mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES,
		// true);
		// mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		//
		// SimpleModule module = new SimpleModule("mod", new Version(1, 0, 0,
		// null));
		// module.addDeserializer(ObjectExpression.class, new
		// ObjectExpressionDeserialiser());
		// mapper.registerModule(module);
		//
		// final String subobj =
		// "{ val4_1: null, val4_2: [{}, true, 'single quote', [1,2,3,false,null,\"double quote\"] ] }";
		// final String root = "{val1: true, val2: false, val3: 'text', val4: "
		// + subobj + "}";
		//
		// ObjectExpression map = mapper.readValue(root,
		// ObjectExpression.class);
		//
		// String expr =
		// "{val1:true,val2:false,val3:'text',val4:{val4_1:null,val4_2:[{}, true, 'single quote', [1, 2, 3, false, null, 'double quote']]}}";
		// assert (expr.equals(map.getExpression(null)));
	}

	private static void setupBullshit()
	{
		Injector injector = Guice.createInjector(new Module()
		{
			@SuppressWarnings({"unchecked", "nls"})
			@Override
			public void configure(Binder arg0)
			{
				arg0.bind(PluginService.class).toInstance(new FakePluginService());
				ParameterizedType type = Types.newParameterizedType(PluginTracker.class, SectionsConverter.class);
				TrackerProvider<SectionsConverter> trackerProvider = new TrackerProvider<SectionsConverter>(
					"", "", "")
				{
					@Override
					public PluginTracker<SectionsConverter> get()
					{
						return new FakePluginTracker();
					}
				};

				TypeLiteral<PluginTracker<SectionsConverter>> typeLiteral = (TypeLiteral<PluginTracker<SectionsConverter>>) TypeLiteral
					.get(type);
				LinkedBindingBuilder<PluginTracker<SectionsConverter>> bindingBuilder = arg0.bind(typeLiteral);
				bindingBuilder.toProvider(trackerProvider).in(Scopes.SINGLETON);
			}
		});
		injector.getInstance(Conversion.class);
	}

	private static class FakePluginTracker extends PluginTracker<SectionsConverter>
	{
		public FakePluginTracker()
		{
			super(new FakePluginService(), "", "", "");
		}

		@Override
		public boolean needsUpdate()
		{
			return false;
		}
	}

	private static class FakePluginService implements PluginService
	{

		@Override
		public PluginBeanLocator getBeanLocator(String pluginId)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public PluginDescriptor getPluginDescriptor(String id)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ExtensionPoint getExtensionPoint(String pluginId, String pointId)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Collection<Extension> getConnectedExtensions(String pluginId, String pointId)
		{
			// TODO Auto-generated method stub
			return Collections.EMPTY_LIST;
		}

		@Override
		public Object getBean(String id, String clazzName)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object getBean(PluginDescriptor plugin, String clazzName)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Iterable<URL> getLocalClassPath(String pluginId)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ClassLoader getClassLoader(PluginDescriptor plugin)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ClassLoader getClassLoader(String pluginId)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void ensureActivated(PluginDescriptor plugin)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void registerExtensionListener(String pluginId, String extensionId, RegistryChangeListener listener)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public String getPluginIdForObject(Object object)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Plugin getPluginForObject(Object object)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isPluginDisabled(TLEPluginLocation location)
		{
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Map<String, TLEPluginLocation> getPluginIdToLocation()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<PluginDescriptor> getAllPluginsAndDependencies(Filter<PluginDescriptor> filter,
			Set<String> disallowed, boolean includeOptional)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void initLocatorsFor(List<Extension> extensions)
		{
			// TODO Auto-generated method stub

		}
	}
}
