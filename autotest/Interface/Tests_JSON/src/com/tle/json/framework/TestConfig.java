package com.tle.json.framework;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import com.google.common.base.Throwables;

@SuppressWarnings("nls")
public class TestConfig
{

	private final Class<?> clazz;
	private final File testFolder;
	private final File configFolder;
	private final File baseFolder = new File("."); // working dir
	private final Properties localServer = new Properties();

	public TestConfig(Class<?> clazz)
	{
		this(clazz, false);
	}

	public TestConfig(Class<?> clazz, boolean noInstitution)
	{
		this.clazz = clazz;

		try
		{
			String location = System.getProperty("interface.tests.location");

			if( location == null )
			{
				// This kind of sucks, if anyone has a better way, go ahead ...
				// Expected to be the <...whatever...>/Interface/Tests full path on
				// your local setup
				File file = new File(System.getProperty("user.home") + "/.tests-location");
				if( !file.exists() )
				{
					file = null;
					if( System.getProperty("tests.location") != null )
					{
						file = new File(System.getProperty("tests.location"));
					}

					if( file == null || !file.exists() )
					{
						this.testFolder = baseFolder;
						this.configFolder = new File(baseFolder, "config");
						return;
					}
				}

				try( BufferedReader br = new BufferedReader(new FileReader(file)) )
				{
					location = br.readLine();
					br.close();
				}
			}

			this.configFolder = new File(location, "config");
			if( !noInstitution )
			{
				this.testFolder = findInstitutionFolder(new File(location));
			}
			else
			{
				this.testFolder = new File(location);
			}
		}
		catch( Exception e )
		{
			throw (Throwables.propagate(e));
		}
	}

	/**
	 * Finds the folder above the "classes" folder for this class
	 * 
	 * @param clazz
	 * @return
	 */
	private File findInstitutionFolder(File parent)
	{
		String folderName = getInstitutionName();
		return new File(parent, folderName);
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

	private Properties readLocalProperties()
	{
		try
		{
			File ls = new File(configFolder, "localserver.properties");
			if( !ls.exists() )
			{
				return new Properties();
			}
			FileInputStream instream = new FileInputStream(ls);
			localServer.load(new InputStreamReader(instream, "UTF-8"));
			instream.close();
			return localServer;
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
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
		return testFolder.getName().endsWith("ssl");
	}

	public int getSslPort()
	{
		String sslPort = getProperty("server.ssl.port");
		if( sslPort == null || sslPort.isEmpty() )
		{
			return 8443;
		}
		return Integer.parseInt(sslPort);
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

	public String getProperty(String property, String defaultVal)
	{
		String propVal = System.getProperty(property);
		if( propVal == null )
		{
			propVal = (String) readLocalProperties().get(property);
			if( propVal == null )
			{
				propVal = defaultVal;
			}
		}
		return propVal;
	}

	public File getScreenshotFolder()
	{
		return new File(baseFolder, "results/screenshots");
	}

	public File getTestFolder()
	{
		return testFolder;
	}

	public String getFirefoxBinary()
	{
		return getProperty("webdriver.firefox.bin");
	}

	public String getGridUrl()
	{
		return getProperty("grid.url");
	}

	public boolean isEquella()
	{
		String prop = getProperty("equella", "false");
		return Boolean.parseBoolean(prop);
	}

	public boolean isChromeDriverSet()
	{
		String chromeDriver = System.getProperty("webdriver.chrome.driver");
		if( chromeDriver == null )
		{
			chromeDriver = (String) readLocalProperties().get("webdriver.chrome.driver");
			if( chromeDriver != null )
			{
				System.setProperty("webdriver.chrome.driver", chromeDriver);
			}
		}
		return !(chromeDriver == null || chromeDriver.isEmpty());
	}

	public String getInstitutionUrlFromShortName(String shortName)
	{
		return getInstitutionUrlFromShortName(shortName, shortName.endsWith("ssl"));
	}

	public String getInstitutionUrlFromShortName(String shortName, boolean https)
	{
		return getServerUrl(https) + shortName + '/';
	}
}
