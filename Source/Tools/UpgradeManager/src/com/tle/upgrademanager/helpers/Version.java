package com.tle.upgrademanager.helpers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;

import com.dytech.common.net.Proxy;
import com.dytech.devlib.Md5;
import com.google.common.base.Function;
import com.google.common.io.CharStreams;
import com.tle.common.Check;
import com.tle.upgrademanager.ManagerConfig;
import com.tle.upgrademanager.ManagerConfig.ManagerDetails;
import com.tle.upgrademanager.Utils;
import com.tle.upgrademanager.handlers.PagesHandler.WebVersion;

@SuppressWarnings("nls")
public class Version
{
	private static final String PROXY_PORT_DEFAULT = "8080";

	private final ManagerConfig config;

	public Version(ManagerConfig config)
	{
		this.config = config;
	}

	public WebVersion getLatestVersion(WebVersion latestDownloadedVersion) throws IOException
	{
		ensureProxy();

		// Get the first upgrade
		String newest = getUrlContent("list", latestDownloadedVersion.getFilename(), null);
		int index = newest.indexOf("\r\n");
		if( index > 0 )
		{
			newest = newest.substring(0, index);
		}

		// If there are upgrades available...
		if( newest.length() > 0 )
		{
			return getWebVersionFromFile(newest);
		}

		return null;
	}

	private String getUrlContent(String method, String oldFileName, String newFileName) throws IOException
	{
		URL url = getUrl(method, oldFileName, newFileName);

		final HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		final int responseCode = conn.getResponseCode();
		if( responseCode == HttpURLConnection.HTTP_UNAUTHORIZED )
		{
			throw new RuntimeException("Invalid username and password for upgrade server");
		}

		final boolean responseOk = responseCode == HttpURLConnection.HTTP_OK;

		try( InputStream in = new BufferedInputStream(responseOk ? conn.getInputStream() : conn.getErrorStream()) )
		{
			StringWriter writer = new StringWriter();
			CharStreams.copy(new InputStreamReader(in), writer);

			String result = writer.toString().trim();
			if( responseOk )
			{
				return result;
			}
			throw new RuntimeException("Error from upgrade server: " + result);
		}
		catch( IOException ex )
		{
			throw new RuntimeException("Error while contacting upgrade server: " + ex.getMessage(), ex);
		}
	}

	private void ensureProxy()
	{
		final ManagerDetails man = config.getManagerDetails();

		String port = man.getProxyPort();
		if( Check.isEmpty(port) )
		{
			port = PROXY_PORT_DEFAULT;
		}
		Proxy.setProxy(man.getProxyHost(), Integer.parseInt(port), man.getProxyUsername(), man.getProxyPassword());
	}

	public WebVersion getUpgradeVersion() throws IOException
	{
		final SortedSet<WebVersion> versions = getVersions();
		if( versions.size() == 0 )
		{
			throw new RuntimeException("No installed versions found.  Please download an upgrade file manually."); //$NON-NLS-1$
		}

		final WebVersion latest = getLatestVersion(versions.first());
		if( latest != null )
		{
			return latest;
		}

		return null;
	}

	public SortedSet<WebVersion> getVersions()
	{
		final SortedSet<WebVersion> versions = new TreeSet<WebVersion>(Utils.VERSION_COMPARATOR);

		final File upgradeFolder = config.getUpdatesDir();
		if( upgradeFolder.exists() )
		{
			for( String file : upgradeFolder.list() )
			{
				if( file != null && Utils.VERSION_EXTRACT.matcher(file).matches() )
				{
					versions.add(getWebVersionFromFile(file));
				}
			}
		}
		else
		{
			throw new RuntimeException("No upgrades folder found");
		}

		return versions;
	}

	private WebVersion getWebVersionFromFile(String fn)
	{
		return new WebVersion(DISPLAY_NAME_ONLY.apply(fn), VERSION_NUMBER_ONLY.apply(fn), FULL_FILENAME.apply(fn));
	}

	public static final Function<String, String> FULL_FILENAME = new Function<String, String>()
	{
		@Override
		public String apply(String filename)
		{
			Matcher m1 = Utils.VERSION_EXTRACT.matcher(filename);
			return m1.matches() ? filename : null;
		}
	};

	public static final Function<String, String> VERSION_NUMBER_ONLY = new Function<String, String>()
	{
		@Override
		public String apply(String filename)
		{
			Matcher m1 = Utils.VERSION_EXTRACT.matcher(filename);
			if( m1.matches() )
			{
				return m1.group(1);
			}
			return null;
		}
	};

	public static final Function<String, String> DISPLAY_NAME_ONLY = new Function<String, String>()
	{
		@Override
		public String apply(String filename)
		{
			Matcher m = Utils.VERSION_EXTRACT.matcher(filename);
			if( m.matches() )
			{
				return m.group(2);
			}
			return null;
		}
	};

	public WebVersion getDeployedVersion()
	{
		WebVersion version = new WebVersion();
		File versionFile = new File(getVersionPropertiesDirectory(), "version.properties");
		try( FileInputStream in = new FileInputStream(versionFile) )
		{
			Properties p = new Properties();
			p.load(in);
			version.setDisplayName(p.getProperty("version.display"));
			version.setMmr(p.getProperty("version.mmr"));
			version.setFilename(MessageFormat.format("tle-upgrade-{0} ({1}).zip", p.getProperty("version.mmr"),
				p.getProperty("version.display")));
		}
		catch( IOException ex )
		{
			version.setDisplayName(Utils.UNKNOWN_VERSION);
		}
		return version;
	}

	private File getVersionPropertiesDirectory()
	{
		return new File(config.getInstallDir(), Utils.EQUELLASERVER_DIR);
	}

	public URL getUrl(String method, String oldFileName, String newFileName)
	{
		final ManagerDetails man = config.getManagerDetails();

		// Check if not configured
		if( Check.isEmpty(man.getUpgradeUsername()) )
		{
			throw new RuntimeException(
				"No username or password set for upgrade server.  Set a username and password on the configuration tab.");
		}

		StringBuilder buffer = new StringBuilder();
		buffer.append("http://");
		buffer.append(man.getUpgradeHost());
		buffer.append("/upgrade/upgrade.do?username=");
		buffer.append(urlEncode(man.getUpgradeUsername()));
		buffer.append("&password=");
		buffer.append(new Md5(man.getUpgradePassword(), "UTF-8").getStringDigest());
		buffer.append("&action=");
		buffer.append(method);
		buffer.append("&old=");
		buffer.append(urlEncode(oldFileName));

		if( newFileName != null )
		{
			buffer.append("&new=");
			buffer.append(urlEncode(newFileName));
		}

		try
		{
			return new URL(buffer.toString());
		}
		catch( MalformedURLException e )
		{
			throw new RuntimeException(e);
		}
	}

	private String urlEncode(String s)
	{
		try
		{
			return URLEncoder.encode(s, "UTF-8");
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException(e);
		}
	}

	public File getUpgradeFile(String filename)
	{
		File vdir = config.getUpdatesDir();
		if( vdir.exists() )
		{
			File file = new File(vdir, filename);
			if( file.exists() )
			{
				return file;
			}
		}

		return null;
	}
}
