package com.tle.core.equella.runner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.PropertyConfigurator;
import org.java.plugin.JpfException;
import org.java.plugin.ObjectFactory;
import org.java.plugin.Plugin;
import org.java.plugin.PluginClassLoader;
import org.java.plugin.PluginManager;
import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.registry.ManifestInfo;
import org.java.plugin.standard.StandardPathResolver;
import org.java.plugin.util.ExtendedProperties;
import org.java.plugin.util.IoUtil;

@SuppressWarnings("nls")
public class EQUELLAServer
{
	private static final Pattern JAR_PATTERN = Pattern.compile("(.*)-\\d+\\.\\d+\\.(\\d+).jar");
	private static final String PLUGIN_JPF_XML = "plugin-jpf.xml";

	private static final Set<String> BOOTIDS = new HashSet<String>(Arrays.asList("jpf:jpf", "log4j:log4j",
		"commons-logging:commons-logging", "org.slf4j:slf4j-api"));

	private static final Collection<String> STARTUP_ROLES = Arrays.asList("initial", "core", "web");

	// Same as Constants.UPGRADE_LOCK
	private static final String UPGRADE_LOCK = "equella.lock";

	public PluginManager manager;

	// JSVC methods (Unix/other)
	public void init(String[] args)
	{
		System.out.println("Initializing EQUELLA Server");
	}

	public void start()
	{
		System.out.println("Starting EQUELLA Server...");
		main(new String[0]);
	}

	public void stop()
	{
		System.out.println("Stopping EQUELLA Server...");
	}

	public void destroy()
	{
		System.out.println("Stopped!");
	}

	// PROCRUN methods (Windows)
	public static void start(String[] args)
	{
		System.out.println("Starting EQUELLA Server...");
		main(new String[0]);
	}

	public static void stop(String[] args)
	{
		System.out.println("Stopping EQUELLA Server...");
	}

	public static void main(String[] args)
	{
		new EQUELLAServer().startServer();
	}

	public void startServer()
	{
		ClassLoader loader = getClass().getClassLoader();

		// // Delete the lock file to signify startup
		URL url = loader.getResource(UPGRADE_LOCK);
		if( url != null )
		{
			try
			{
				Files.deleteIfExists(Paths.get(url.toURI()));
			}
			catch( IOException | URISyntaxException e )
			{
				// Do not stop - But unable to delete equella.lock
			}
		}

		PropertyConfigurator.configure(loader.getResource("learningedge-log4j.properties"));
		Properties mandatory = new Properties();
		Properties optional = new Properties();

		try
		{
			TomcatLogRedirector.activate();
			mandatory.load(loader.getResourceAsStream("mandatory-config.properties"));
			optional.load(loader.getResourceAsStream("optional-config.properties"));

			ExtendedProperties props = new ExtendedProperties();
			String pathResolver;
			if( Boolean.getBoolean("equella.devmode") )
			{
				pathResolver = StandardPathResolver.class.getName();
			}
			else
			{
				pathResolver = CleaningShadingPathResolver.class.getName();
			}
			props.setProperty("org.java.plugin.PathResolver", pathResolver);
			props.setProperty("org.java.plugin.standard.StandardPluginLifecycleHandler.probeParentLoaderLast", "false");
			ObjectFactory objectFactory = ObjectFactory.newInstance(props);

			List<String> folders = Arrays.asList(mandatory.getProperty("plugins.location"));
			manager = objectFactory.createManager();

			Map<String, TLEPluginLocation> registered = new HashMap<String, TLEPluginLocation>();
			publishFakes(registered, "commons-logging.xml", "jpf.xml", "log4j.xml", "slf4j-api.xml", "slf4j-log4j.xml");
			scanPluginFolders(registered, folders, BOOTIDS);
			manager.publishPlugins(registered.values().toArray(new PluginLocation[registered.size()]));
			List<Object[]> alreadyRegistered = new ArrayList<Object[]>();
			for( TLEPluginLocation pluginLocation : registered.values() )
			{
				Object[] entry = new Object[]{pluginLocation.getManifestInfo(), pluginLocation.getJar(),
						pluginLocation.getVersion(), pluginLocation.getContextLocation(),
						pluginLocation.getManifestLocation()};
				alreadyRegistered.add(entry);
			}
			Plugin plugin = manager.getPlugin("com.tle.core.application");
			PluginClassLoader plugLoader = manager.getPluginClassLoader(plugin.getDescriptor());
			Class<?> clazz = plugLoader.loadClass("com.tle.core.application.ApplicationStarter");
			Method method = clazz.getMethod("start", PluginManager.class, Collection.class, Collection.class);
			method.invoke(null, manager, alreadyRegistered, STARTUP_ROLES);
		}
		catch( Exception e )
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void publishFakes(Map<String, TLEPluginLocation> registered, String... filenames) throws JpfException
	{
		for( String filename : filenames )
		{
			URL resource = getClass().getResource(filename);
			ManifestInfo manifestInfo = manager.getRegistry().readManifestInfo(resource);
			registered.put(manifestInfo.getId(), new TLEPluginLocation(manifestInfo, null, resource, resource));
		}
	}

	private void scanPluginFolders(Map<String, TLEPluginLocation> registered, Collection<String> pluginFolders,
		Set<String> disallowedPlugins) throws Exception
	{
		for( String folder : pluginFolders )
		{
			String[] folders = folder.split(",");
			for( String folderName : folders )
			{
				File file = new File(folderName.trim());
				if( file.exists() )
				{
					scanPluginFolder(file, registered, disallowedPlugins);
				}
			}
		}
	}

	private void scanPluginFolder(File folder, Map<String, TLEPluginLocation> plugins, Set<String> disallowedPlugins)
		throws Exception
	{
		File[] files = folder.listFiles();
		if( files == null )
		{
			return;
		}

		for( File file : files )
		{
			String filename = file.getName();
			if( file.isDirectory() )
			{
				File manFile = new File(file, PLUGIN_JPF_XML);
				if( manFile.exists() )
				{
					URL context = file.toURI().toURL();
					URL manUrl = new URL(context, PLUGIN_JPF_XML);
					ManifestInfo info = manager.getRegistry().readManifestInfo(manUrl);
					String pluginId = info.getId();
					if( !disallowedPlugins.contains(pluginId) )
					{
						TLEPluginLocation location = new TLEPluginLocation(info, filename, context, manUrl);
						plugins.put(pluginId, location);
					}
				}
				else
				{
					scanPluginFolder(file, plugins, disallowedPlugins);
				}
			}
			else if( filename.endsWith(".jar") )
			{
				int version = -1;
				String pluginId = filename;
				Matcher matcher = JAR_PATTERN.matcher(filename);
				if( matcher.matches() )
				{
					pluginId = matcher.group(1);
					version = Integer.parseInt(matcher.group(2));
				}
				URL context = new URL("jar", "", file.toURI() + "!/");
				URL manFile = new URL(context, PLUGIN_JPF_XML);
				if( IoUtil.isResourceExists(manFile) )
				{
					ManifestInfo info = manager.getRegistry().readManifestInfo(manFile);
					pluginId = info.getId();
					if( !disallowedPlugins.contains(pluginId) )
					{
						TLEPluginLocation location = new TLEPluginLocation(info, filename, context, manFile);
						location.setVersion(version);
						TLEPluginLocation prevLocation = plugins.get(pluginId);
						if( prevLocation == null || prevLocation.getVersion() < version )
						{
							plugins.put(pluginId, location);
						}
					}
				}
			}
		}
	}

	public static class TLEPluginLocation implements org.java.plugin.PluginManager.PluginLocation
	{
		private final String jar;
		private final ManifestInfo manifestInfo;
		private final URL context;
		private final URL manifest;
		private int version = -1;

		@Override
		public boolean equals(Object obj)
		{
			if( this == obj )
			{
				return true;
			}

			if( !(obj instanceof TLEPluginLocation) )
			{
				return false;
			}

			return context.toString().equals(((TLEPluginLocation) obj).context.toString());
		}

		@Override
		public int hashCode()
		{
			return context.toString().hashCode();
		}

		public TLEPluginLocation(ManifestInfo info, String jar, URL context, URL manifest)
		{
			this.manifestInfo = info;
			this.jar = jar;
			this.context = context;
			this.manifest = manifest;
		}

		public int getVersion()
		{
			return version;
		}

		public String getJar()
		{
			return jar;
		}

		@Override
		public URL getContextLocation()
		{
			return context;
		}

		@Override
		public URL getManifestLocation()
		{
			return manifest;
		}

		public void setVersion(int version)
		{
			this.version = version;
		}

		public ManifestInfo getManifestInfo()
		{
			return manifestInfo;
		}
	}
}
