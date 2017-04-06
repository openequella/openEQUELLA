package com.tle.upgrademanager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Properties;

import com.tle.common.Check;
import com.tle.common.util.EquellaConfig;

@SuppressWarnings("nls")
public class ManagerConfig extends EquellaConfig
{
	private final File updatesDir;

	private final ManagerDetails managerDetails;

	public ManagerConfig(File installPath, InputStream inputStream) throws IOException
	{
		super(installPath);
		updatesDir = new File(managerDir, "updates"); //$NON-NLS-1$
		managerDetails = new ManagerDetails(new File(managerDir, "config.properties"), inputStream); //$NON-NLS-1$
	}

	public File getUpdatesDir()
	{
		return updatesDir;
	}

	public ManagerDetails getManagerDetails()
	{
		return managerDetails;
	}

	public class ManagerDetails
	{
		private static final String UPGRADE_HOST_KEY = "upgrade.host";
		private static final String UPGRADE_USERNAME_KEY = "upgrade.username";
		private static final String UPGRADE_PASSWORD_KEY = "upgrade.password";

		private static final String PROXY_HOST_KEY = "proxy.host";
		private static final String PROXY_PORT_KEY = "proxy.port";
		private static final String PROXY_USERNAME_KEY = "proxy.username";
		private static final String PROXY_PASSWORD_KEY = "proxy.password";

		private static final String SERVER_PORT_KEY = "server.port";
		private static final String LOADING_TIME_OUT_KEY = "loading.time.out";

		private static final String LOADING_TIME_OUT_DEFAULT = "6000";
		private static final String SERVER_PORT_DEFAULT = "3000";
		private static final String UPGRADE_HOST_DEFAULT = "trial.thelearningedge.com.au";

		private static final String VERSION_MMR = "version.mmr";
		private static final String VERSION_DISPLAY = "version.display";
		private static final String VERSION_COMMIT = "version.commit";

		private final File managerConfigFile;
		private final Properties props;

		// sort-of final
		private int managerPort;
		private long loadingTimeout;
		private String upgradeHost;

		private String proxyHost;
		private String proxyPort;
		private String proxyUsername;
		private String proxyPassword;
		private String upgradeUsername;
		private String upgradePassword;

		private final String fullVersion;

		protected ManagerDetails(File managerConfigFile, InputStream inputStream) throws IOException
		{
			this.managerConfigFile = managerConfigFile;
			this.props = new Properties();

			load();

			if( inputStream != null )
			{
				final Properties vp = new Properties();
				vp.load(inputStream);
				fullVersion = MessageFormat.format("{0} {1} ({2})", vp.getProperty(VERSION_MMR),
					vp.getProperty(VERSION_DISPLAY), vp.getProperty(VERSION_COMMIT));
			}
			else
			{
				fullVersion = "Unknown";
			}
		}

		public void validate() throws Exception
		{
			validateProxyPort();
			validateUsernamePassword();
		}

		private void validateProxyPort() throws Exception
		{
			if( proxyPort != null && proxyPort.trim().length() > 0 )
			{
				try
				{
					Integer.parseInt(proxyPort);
				}
				catch( NumberFormatException n )
				{
					throw new Exception("Proxy port must be a number value");
				}
			}
		}

		private void validateUsernamePassword() throws Exception
		{
			// username with no password?
			if( !Check.isEmpty(upgradeUsername) && Check.isEmpty(upgradePassword) )
			{
				throw new Exception("Upgrade username is configured but no upgrade password is configured");
			}

			// password with no username?
			if( Check.isEmpty(upgradeUsername) && !Check.isEmpty(upgradePassword) )
			{
				throw new Exception("Upgrade password is configured but no upgrade username is configured");
			}
		}

		public final void load() throws IOException
		{
			try( Reader reader = new FileReader(this.managerConfigFile) )
			{
				props.load(reader);

				proxyHost = props.getProperty(PROXY_HOST_KEY);
				proxyPort = props.getProperty(PROXY_PORT_KEY);
				proxyUsername = props.getProperty(PROXY_USERNAME_KEY);
				proxyPassword = props.getProperty(PROXY_PASSWORD_KEY);
				upgradeUsername = props.getProperty(UPGRADE_USERNAME_KEY);
				upgradePassword = props.getProperty(UPGRADE_PASSWORD_KEY);

				managerPort = Integer.parseInt(props.getProperty(SERVER_PORT_KEY, SERVER_PORT_DEFAULT));
				loadingTimeout = Long.parseLong(props.getProperty(LOADING_TIME_OUT_KEY, LOADING_TIME_OUT_DEFAULT));
				upgradeHost = props.getProperty(UPGRADE_HOST_KEY, UPGRADE_HOST_DEFAULT);
			}
		}

		public void save() throws Exception
		{
			validate();

			try( Writer writer = new FileWriter(managerConfigFile) )
			{
				// these are the only changeable properties
				props.setProperty(PROXY_HOST_KEY, proxyHost);
				props.setProperty(PROXY_PORT_KEY, proxyPort);
				props.setProperty(PROXY_USERNAME_KEY, proxyUsername);
				props.setProperty(PROXY_PASSWORD_KEY, proxyPassword);
				props.setProperty(UPGRADE_USERNAME_KEY, upgradeUsername);
				props.setProperty(UPGRADE_PASSWORD_KEY, upgradePassword);

				props.store(writer, "");
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}
		}

		public int getManagerPort()
		{
			return managerPort;
		}

		public long getLoadingTimeout()
		{
			return loadingTimeout;
		}

		public String getUpgradeHost()
		{
			return upgradeHost;
		}

		public String getFullVersion()
		{
			return fullVersion;
		}

		public String getProxyHost()
		{
			return proxyHost;
		}

		public void setProxyHost(String proxyHost)
		{
			this.proxyHost = proxyHost;
		}

		public String getProxyPort()
		{
			return proxyPort;
		}

		public void setProxyPort(String proxyPort)
		{
			this.proxyPort = proxyPort;
		}

		public String getProxyUsername()
		{
			return proxyUsername;
		}

		public void setProxyUsername(String proxyUsername)
		{
			this.proxyUsername = proxyUsername;
		}

		public String getProxyPassword()
		{
			return proxyPassword;
		}

		public void setProxyPassword(String proxyPassword)
		{
			this.proxyPassword = proxyPassword;
		}

		public String getUpgradeUsername()
		{
			return upgradeUsername;
		}

		public void setUpgradeUsername(String upgradeUsername)
		{
			this.upgradeUsername = upgradeUsername;
		}

		public String getUpgradePassword()
		{
			return upgradePassword;
		}

		public void setUpgradePassword(String upgradePassword)
		{
			this.upgradePassword = upgradePassword;
		}
	}
}
