package com.tle.webtests.framework;

import com.google.common.base.Throwables;
import com.tle.common.Check;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.fest.util.Strings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Properties;

public class TestConfig
{
	private static final String INSTITUTION_PROPS = "institution.properties";
	private static final Config config = ConfigFactory.load();

	private final Class<?> clazz;
	private final File testFolder;
	private final File baseFolder;
	private Properties localServer;
	private final boolean noInstitution;

	public TestConfig(Class<?> clazz)
	{
		this(clazz, false);
	}

	public TestConfig(Class<?> clazz, boolean noInstitution)
	{
		URL configPathUrl = getClass().getResource("/application.conf.example");
		this.baseFolder = Paths.get(URI.create(configPathUrl.toString())).getParent().getParent().toFile();
		this.clazz = clazz;
		this.noInstitution = noInstitution;
		if( !noInstitution )
		{
			this.testFolder = findInstitutionFolder();
		}
		else
		{
			this.testFolder = baseFolder;
		}
	}

	public boolean isNoInstitution() {
		return noInstitution;
	}

	/**
	 * Finds the folder above the "classes" folder for this class
	 * 
	 * @return
	 */
	private File findInstitutionFolder()
	{
		String folderName = getInstitutionName();
		return findInstitutionFolder(folderName);
	}

	private File findInstitutionFolder(String name)
	{
		return new File(baseFolder, "tests/" + name);
	}

	public String getInstitutionName()
	{
		String inst;
		TestInstitution annotation = clazz.getAnnotation(TestInstitution.class);
		if( annotation == null )
		{
			throw new Error("Tests must be annotated with @TestInstitution now");
		}
		else
		{
			inst = annotation.value();
		}

		return inst;
	}

	public int getStandardTimeout()
	{
		return getIntProperty("timeout.standard", 30);
	}

	public String getAdminPassword()
	{
		return getProperty("server.password");
	}

	public String getAdminUrl()
	{
		String adminUrl = getProperty("admin.url");
		if( adminUrl == null )
		{
			return getServerUrl();
		}
		return adminUrl;
	}

	public String getServerUrl()
	{
		return getServerUrl(isSsl());
	}

	public String getServerUrl(boolean ssl)
	{
		String serverUrl = getProperty("server.url");
		if( !serverUrl.endsWith("/") )
		{
			serverUrl += '/';
		}
		if( ssl )
		{
			try
			{
				URL url = new URL(serverUrl);
				int sslPort = getSslPort();
				if( sslPort == 443 )
				{
					serverUrl = new URL("https", url.getHost(), url.getFile()).toExternalForm();
				}
				else
				{
					serverUrl = new URL("https", url.getHost(), sslPort, url.getFile()).toExternalForm();
				}
			}
			catch( MalformedURLException e )
			{
				throw Throwables.propagate(e);
			}
		}
		return serverUrl;
	}

	public boolean isSsl()
	{
		return isSsl(testFolder);
	}

	public boolean isSsl(File testFolder)
	{
		boolean https = testFolder.getName().endsWith("ssl");
		if( !https )
		{
			try
			{
				Properties props = getInstProperties(testFolder);
				if( props != null )
				{
					https = Boolean.parseBoolean(props.getProperty("https", "false"));
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		return https;
	}

	public int getSslPort()
	{
		String sslPort = getProperty("server.ssl.port");
		if( Check.isEmpty(sslPort) )
		{
			return 8443;
		}
		return Integer.parseInt(sslPort);
	}

	public String getMoodleUrl(String version)
	{
		String serverUrl = getProperty("moodle." + version + ".url");
		if( serverUrl != null && !serverUrl.endsWith("/") )
		{
			serverUrl += '/';
		}
		return serverUrl;
	}

	public String getMoodleContextUrl(String version)
	{
		String moodleUrl = getMoodleUrl(version);
		return Strings.isEmpty(moodleUrl) ? moodleUrl : MessageFormat.format("{0}moodle{1}/", moodleUrl, version);
	}

	public String getIntegrationUrl(String integ)
	{
		String serverUrl = getProperty(integ + ".url");
		if( serverUrl != null && !serverUrl.endsWith("/") )
		{
			serverUrl += '/';
		}
		return serverUrl;
	}

	public String getProperty(String property)
	{
		return getProperty(property, null);
	}

	public String getProperty(String property, String defaultValue)
	{
		if (config.hasPath(property)) {
			return config.getString(property);
		} else {
			return defaultValue;
		}
	}

	public int getIntProperty(String property, int defaultValue)
	{
		String val = getProperty(property);
		if( !Check.isEmpty(val) )
		{
			try
			{
				return Integer.parseInt(val);
			}
			catch( NumberFormatException ex )
			{
				// Fall through
			}
		}
		return defaultValue;
	}

	public boolean getBooleanProperty(String property, boolean defaultValue)
	{
		String val = getProperty(property);
		if( !Check.isEmpty(val) )
		{
			return Boolean.parseBoolean(val);
		}
		return defaultValue;
	}

	public File getScreenshotFolder()
	{
		return new File(baseFolder, "target/results/screenshots");
	}

	public File getTestFolder()
	{
		return testFolder;
	}

	public String getChromeBinary()
	{
		return getProperty("webdriver.chrome.bin");
	}

	public String getFirefoxBinary()
	{
		return getProperty("webdriver.firefox.bin");
	}

	public String getGridUrl()
	{
		return getProperty("grid.url");
	}

	public boolean isChromeDriverSet()
	{
		String chromeDriver = System.getProperty("webdriver.chrome.driver");
		if( chromeDriver == null )
		{
			chromeDriver = getProperty("webdriver.chrome.driver");

			if( chromeDriver != null )
			{
				System.setProperty("webdriver.chrome.driver", chromeDriver);
			}

			String chromeLog = getProperty("webdriver.chrome.logfile");
			if( chromeLog != null )
			{
				System.setProperty("webdriver.chrome.logfile", chromeLog);
			}

			String chromeVerboseLogging = getProperty("webdriver.chrome.verboseLogging");
			if( chromeVerboseLogging != null )
			{
				System.setProperty("webdriver.chrome.verboseLogging", chromeVerboseLogging);
			}
		}
		return !Check.isEmpty(chromeDriver);
	}

	private static Properties getInstProperties(File instFolder) throws IOException
	{
		File propsFile = new File(instFolder, INSTITUTION_PROPS);
		Properties props = null;
		if( propsFile.exists() )
		{
			FileInputStream fis = new FileInputStream(propsFile);
			try
			{
				props = new Properties();
				props.load(fis);
			}
			finally
			{
				fis.close();
			}
		}
		return props;
	}

	//
	//	public String getInstitutionUrl()
	//	{
	//		return getServerUrl(isSsl()) + testFolder.getName() + '/';
	//	}

	public String getInstitutionUrl()
	{
		return getInstitutionUrl(testFolder);
	}

	private String getInstitutionUrl(File instFolder)
	{
		boolean ssl = isSsl(instFolder);
		return getInstitutionUrl(instFolder.getName(), ssl);
	}

	public String getInstitutionUrl(String instName)
	{
		File instFolder = findInstitutionFolder(instName);
		return getInstitutionUrl(instFolder);
	}

	public String getInstitutionUrl(String instName, boolean https)
	{
		return getServerUrl(https) + instName + '/';
	}

	//
	//	public String getInstitutionUrlFromShortName(String shortName)
	//	{
	//		return getInstitutionUrlFromShortName(shortName, shortName.endsWith("ssl"));
	//	}
	//
	//	public String getInstitutionUrlFromShortName(String shortName, boolean https)
	//	{
	//		return getServerUrl(https) + shortName + '/';
	//	}
}
