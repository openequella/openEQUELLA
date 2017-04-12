package com.tle.blackboard.buildingblock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import blackboard.data.blti.BasicLTIDomainConfig;
import blackboard.data.blti.BasicLTIDomainConfig.SendUserData;
import blackboard.data.blti.BasicLTIDomainConfig.Status;
import blackboard.data.blti.BasicLTIDomainHost;
import blackboard.data.blti.BasicLTIPlacement;
import blackboard.persist.Id;
import blackboard.platform.blti.BasicLTIDomainConfigManager;
import blackboard.platform.blti.BasicLTIDomainConfigManagerFactory;
import blackboard.platform.gradebook2.ScoreProvider;
import blackboard.platform.gradebook2.impl.ScoreProviderDAO;
import blackboard.platform.plugin.ContentHandler;
import blackboard.platform.plugin.ContentHandlerDbLoader;
import blackboard.platform.plugin.ContentHandlerDbPersister;
import blackboard.platform.plugin.PlugInConfig;
import blackboard.platform.vxi.data.VirtualHost;
import blackboard.platform.vxi.service.VirtualInstallationManager;
import blackboard.platform.vxi.service.VirtualInstallationManagerFactory;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import com.tle.blackboard.common.BbContext;
import com.tle.blackboard.common.BbUtil;
import com.tle.blackboard.common.PathUtils;
import com.tle.blackboard.common.content.PlacementUtil;
import com.tle.blackboard.common.content.PlacementUtil.LoadPlacementResponse;

@SuppressWarnings("nls")
// @NonNullByDefault
public class Configuration
{
	/* @Nullable */
	private static Configuration instance;
	private static final Object instanceLock = new Object();

	private static final String CONFIG_FILE = "config.properties";
	private static final String CONTENT_HANDLER_HANDLE = "resource/tle-resource";

	// DEPRECATED CONFIG ELEMENTS
	private static final String HOST = "host";
	private static final String PORT = "port";
	private static final String CONTEXT = "context";
	private static final String INSTITUTION = "institution";
	private static final String SECURE = "secure";
	private static final String MOCK_PORTAL_ROLES = "mock.portal.roles";

	// CONFIG ELEMENTS (referred to in the JSP)
	public static final String EQUELLA_URL = "equellaurl";
	public static final String SECRET = "secret";
	public static final String SECRETID = "secretid";
	public static final String OAUTH_CLIENT_ID = "oauth.clientid";
	public static final String OAUTH_CLIENT_SECRET = "oauth.clientsecret";
	public static final String RESTRICTIONS = "restrictions";
	public static final String NEWWINDOW = "newwindow";

	private static final String DEFAULT_SECRET = "";
	private static final String DEFAULT_RESTRICTION = "none";

	private final BbContext context;
	private final PlugInConfig plugin;
	/* @Nullable */
	private ContentHandler contentHandler;
	private final Object contentHandlerLock = new Object();

	/* @Nullable */
	private String version;
	/* @Nullable */
	private String equellaUrl;
	/* @Nullable */
	private String secret;
	/* @Nullable */
	private String secretid;
	/* @Nullable */
	private String oauthClientId;
	/* @Nullable */
	private String oauthClientSecret;
	/* @Nullable */
	private String restriction;
	private boolean newWindow;
	/* @Nullable */
	private Set<String> mockPortalRoles;

	private Date lastModified = Calendar.getInstance().getTime();

	@SuppressWarnings("null")
	public static Configuration instance()
	{
		if( instance == null )
		{
			synchronized( instanceLock )
			{
				if( instance == null )
				{
					instance = new Configuration();
				}
			}
		}
		return instance;
	}

	private Configuration()
	{
		try
		{
			plugin = new PlugInConfig(BbUtil.VENDOR, BbUtil.HANDLE);
			context = BbContext.instance();

			final VirtualInstallationManager vim = VirtualInstallationManagerFactory.getInstance();
			final VirtualHost vhost = vim.getVirtualHost("");
			context.getContextManager().setContext(vhost);

			version = loadVersion();
			BbUtil.trace("Version: " + version);

			load();
			ensureLtiPlacement();
			ensureScoreProvider();
			// Fix up dodgy Blind SSL
			// HttpsURLConnection.setDefaultSSLSocketFactory(new
			// sun.security.ssl.SSLSocketFactoryImpl());
		}
		catch( Exception e )
		{
			BbUtil.error("Couldn't init building block", e);
			throw Throwables.propagate(e);
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		context.getContextManager().releaseContext();
		super.finalize();
	}

	public synchronized void modify(HttpServletRequest request) throws Exception
	{
		setEquellaUrl(request.getParameter(EQUELLA_URL));
		setSecret(request.getParameter(SECRET));
		setSecretId(request.getParameter(SECRETID));
		setOauthClientId(request.getParameter(OAUTH_CLIENT_ID));
		setOauthClientSecret(request.getParameter(OAUTH_CLIENT_SECRET));
		setRestriction(request.getParameter(RESTRICTIONS));
		String newWindowParam = request.getParameter(NEWWINDOW);
		if( newWindowParam == null || newWindowParam.equals("") )
		{
			newWindowParam = "false";
		}
		setNewWindow(Boolean.parseBoolean(newWindowParam));
		lastModified = Calendar.getInstance().getTime();
	}

	public synchronized void load()
	{
		final File configFile = new File(getConfigDirectory(), CONFIG_FILE);
		if( !configFile.exists() )
		{
			try
			{
				configFile.createNewFile();
				BbUtil.trace("Successfully created configuration file");
			}
			catch( IOException e )
			{
				BbUtil.error("Error creating configuration file", e);
				throw Throwables.propagate(e);
			}
			return;
		}

		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(configFile);
			final Properties props = new Properties();
			props.load(fis);
			if( props.containsKey(EQUELLA_URL) )
			{
				setEquellaUrl(props.getProperty(EQUELLA_URL));
			}
			else
			{
				try
				{
					setEquellaUrl(buildEquellaUrlFromDeprecatedConfigParams(props));
				}
				catch( MalformedURLException mal )
				{
					BbUtil.trace("Failed to load equella URL from deprecated props");
				}
			}
			setSecret(props.getProperty(SECRET));
			setSecretId(props.getProperty(SECRETID));
			setOauthClientId(props.getProperty(OAUTH_CLIENT_ID));
			setOauthClientSecret(props.getProperty(OAUTH_CLIENT_SECRET));
			setMockPortalRoles(commaSplit(props.getProperty(MOCK_PORTAL_ROLES)));
			setRestriction(props.getProperty(RESTRICTIONS));
			setNewWindow(Boolean.parseBoolean(props.getProperty(NEWWINDOW, "true")));
		}
		catch( Exception e )
		{
			BbUtil.error("Error loading configuration", e);
			throw Throwables.propagate(e);
		}
		finally
		{
			if( fis != null )
			{
				try
				{
					fis.close();
				}
				catch( IOException e )
				{
					// Ignore
				}
			}
		}
	}

	public synchronized void save()
	{
		final File configFile = new File(getConfigDirectory(), CONFIG_FILE);
		FileOutputStream fos = null;
		try
		{
			ensureLtiPlacement();
			ensureScoreProvider();

			fos = new FileOutputStream(configFile);

			Properties props = new Properties();
			props.setProperty(EQUELLA_URL, equellaUrl);
			props.setProperty(SECRET, secret);
			props.setProperty(SECRETID, secretid);
			props.setProperty(OAUTH_CLIENT_ID, oauthClientId);
			props.setProperty(OAUTH_CLIENT_SECRET, oauthClientSecret);
			props.setProperty(MOCK_PORTAL_ROLES, commaJoin(mockPortalRoles));
			props.setProperty(RESTRICTIONS, restriction);
			props.setProperty(NEWWINDOW, Boolean.toString(newWindow));
			props.store(fos, null);
		}
		catch( Exception e )
		{
			BbUtil.error("Error saving configuration", e);
			throw Throwables.propagate(e);
		}
		finally
		{
			if( fos != null )
			{
				try
				{
					fos.close();
				}
				catch( IOException e )
				{
					// Ignore
				}
			}
		}
	}

	/**
	 * @Nullable
	 * @return
	 */
	private synchronized BasicLTIPlacement ensureLtiPlacement()
	{
		try
		{
			BbUtil.trace("ensureLtiPlacement");
			if( Strings.isNullOrEmpty(oauthClientId) || Strings.isNullOrEmpty(oauthClientSecret)
				|| Strings.isNullOrEmpty(equellaUrl) )
			{
				BbUtil.trace("Not creating a placement since a property was blank");
				return null;
			}

			BasicLTIDomainConfig domainConfig = null;
			boolean newPlacement = false;
			BasicLTIPlacement placement = PlacementUtil.loadFromHandle(CONTENT_HANDLER_HANDLE);
			if( placement == null )
			{
				BbUtil.trace("Loading placement via URL " + equellaUrl);
				final LoadPlacementResponse loadPlacement = PlacementUtil.loadPlacementByUrl(equellaUrl);
				placement = loadPlacement.getPlacement();
				domainConfig = loadPlacement.getDomainConfig();
				if( placement == null )
				{
					BbUtil.trace("No existing placement for URL " + equellaUrl);
					final Id placementId = getContentHandler().getBasicLTIPlacementId();
					if( !Id.isValid(placementId) )
					{
						BbUtil.trace("No existing placement associated with ContentHandler");

						if( domainConfig == null )
						{
							domainConfig = createDomainConfig();
						}
						placement = PlacementUtil.createNewPlacement(domainConfig, getContentHandler());
						newPlacement = true;
					}
					else
					{
						BbUtil.trace("Loading existing placement from ContentHandler");
						placement = PlacementUtil.loadFromId(placementId);
						if( placement == null )
						{
							BbUtil.trace("Content handler pointing to invalid placement...");
							if( domainConfig == null )
							{
								domainConfig = createDomainConfig();
							}
							placement = PlacementUtil.createNewPlacement(domainConfig, getContentHandler());
							newPlacement = true;
						}
					}
				}
				else
				{
					BbUtil.trace("Loaded placement via URL");
				}
			}
			else
			{
				BbUtil.trace("Loaded placement via handle");
			}

			// ensure domainConfig
			if( domainConfig == null )
			{
				BbUtil.trace("No domain config loaded");

				// get any current domain config for this domain
				domainConfig = PlacementUtil.loadDomainConfigByUrl(new URL(equellaUrl));

				if( domainConfig != null )
				{
					BbUtil.trace("Domain config loaded via URL " + equellaUrl);
					if( populateDomainConfig(domainConfig) )
					{
						BbUtil.trace("Saving dirty domain config");
						final BasicLTIDomainConfigManager fac = BasicLTIDomainConfigManagerFactory.getInstance();
						fac.save(domainConfig);
					}
					else
					{
						BbUtil.trace("Not saving doman config (not dirty)");
					}
				}
				else
				{
					BbUtil.trace("No domain config for URL " + equellaUrl);
					final Id domainConfigId = placement.getBasicLTIDomainConfigId();
					if( Id.isValid(domainConfigId) )
					{
						BbUtil.trace("Loading domain config based on placement pointer");
						final BasicLTIDomainConfigManager fac = BasicLTIDomainConfigManagerFactory.getInstance();
						domainConfig = fac.loadById(domainConfigId);
						if( populateDomainConfig(domainConfig) )
						{
							BbUtil.trace("Saving dirty domain config");
							fac.save(domainConfig);
						}
						else
						{
							BbUtil.trace("Not saving domain config (not dirty)");
						}
					}
					else
					{
						BbUtil.trace("Creating new domain config");
						domainConfig = createDomainConfig();
					}
				}
			}

			BbUtil.trace("newPlacement = " + newPlacement);
			BbUtil.trace("placement = " + placement.getId().toExternalString());

			boolean saveHandler = newPlacement;
			// delete the old placement associated with this handler if
			// there is one
			final ContentHandler handler = getContentHandler();
			if( handlerPlacementMismatch(placement, handler) )
			{
				if( Id.isValid(handler.getBasicLTIPlacementId()) )
				{
					BbUtil.trace("Deleting existing placement associated with this handler "
						+ handler.getBasicLTIPlacementId().toExternalString());
					PlacementUtil.deleteById(handler.getBasicLTIPlacementId());
				}
				saveHandler = true;
			}

			// Save placement?
			// if( newPlacement || requiresSave(placement, domainConfig) )
			// {
			placement.setBasicLTIDomainConfigId(domainConfig.getId());
			placement.setUrl(equellaUrl);
			PlacementUtil.save(placement);
			// }

			// Save handler?
			if( saveHandler )
			{
				BbUtil.trace("Saving updated ContentHandler with placement " + placement.getId().toExternalString());

				handler.setBasicLTIPlacementId(placement.getId());
				final ContentHandlerDbPersister contentHandlerPersister = (ContentHandlerDbPersister) BbContext
					.instance().getPersistenceManager().getPersister(ContentHandlerDbPersister.TYPE);
				contentHandlerPersister.persist(handler);
			}
			else
			{
				BbUtil.trace("Not saving ContentHandler");
			}

			return placement;
		}
		catch( Exception e )
		{
			BbUtil.error("Error ensuring LTI placement", e);
			throw Throwables.propagate(e);
		}
	}

	private boolean handlerPlacementMismatch(BasicLTIPlacement placement, ContentHandler handler)
	{
		final Id currentPlacementId = handler.getBasicLTIPlacementId();
		if( !currentPlacementId.equals(placement.getId()) )
		{
			BbUtil.trace("ContentHandler is pointing at a different (or null) placement "
				+ handler.getBasicLTIPlacementId().toExternalString());
			return true;
		}
		BbUtil.trace("ContentHandler is pointing to correct placement");
		return false;
	}

	private synchronized ScoreProvider ensureScoreProvider()
	{
		final ScoreProviderDAO dao = ScoreProviderDAO.get();
		ScoreProvider provider = dao.getByHandle(BbUtil.CONTENT_HANDLER);
		if( provider == null )
		{
			// The boolean values are cloned from what I could find about resource/x-bb-blti-link
			// in score_provider.txt in BB installer
			provider = new ScoreProvider();
			provider.setName("Equella");
			provider.setHandle(BbUtil.CONTENT_HANDLER);
			provider.setAllowAttemptGrading(true);
			provider.setAllowMultiple(false);
			provider.setAttemptBased(false);
			provider.setGradeAction(PathUtils.urlPath(BbUtil.getBlockRelativePath(), "ViewGradebook"));
			provider.setReviewAction(PathUtils.urlPath(BbUtil.getBlockRelativePath(), "ViewGradebook"));
			dao.persist(provider);
		}

		return provider;
	}

	private BasicLTIDomainConfig createDomainConfig()
	{
		final BasicLTIDomainConfig domainConfig = new BasicLTIDomainConfig();
		populateDomainConfig(domainConfig);
		domainConfig.setSendEmail(true);
		domainConfig.setSendName(true);
		domainConfig.setSendRole(true);
		domainConfig.setUseSplash(false);
		domainConfig.setSendUserData(SendUserData.Always);
		domainConfig.setStatus(Status.Approved);
		final BasicLTIDomainConfigManager fac = BasicLTIDomainConfigManagerFactory.getInstance();
		fac.save(domainConfig);
		return domainConfig;
	}

	/**
	 * @param domainConfig
	 * @return Was changed
	 */
	private boolean populateDomainConfig(BasicLTIDomainConfig domainConfig)
	{
		try
		{
			boolean dirty = false;
			final String key = domainConfig.getKey();
			if( !Strings.nullToEmpty(key).equals(Strings.nullToEmpty(oauthClientId)) )
			{
				domainConfig.setKey(oauthClientId);
				dirty = true;
			}
			final String secret = domainConfig.getSecret();
			if( !Strings.nullToEmpty(secret).equals(Strings.nullToEmpty(oauthClientSecret)) )
			{
				domainConfig.setSecret(oauthClientSecret);
				dirty = true;
			}

			BasicLTIDomainHost host = domainConfig.getPrimaryHost();
			final String newDomain = new URL(equellaUrl).getHost();
			if( host == null || !Strings.nullToEmpty(host.getDomain()).equals(Strings.nullToEmpty(newDomain)) )
			{
				host = (host == null ? new BasicLTIDomainHost() : host);
				host.setDomain(newDomain);
				host.setPrimary(true);
				domainConfig.setPrimaryHost(host);
				dirty = true;
			}
			return dirty;
		}
		catch( Exception mal )
		{
			BbUtil.error("Error populating domainConfig", mal);
			throw Throwables.propagate(mal);
		}
	}

	private ContentHandler getContentHandler() throws Exception
	{
		if( contentHandler == null )
		{
			synchronized( contentHandlerLock )
			{
				if( contentHandler == null )
				{
					final ContentHandlerDbLoader contentHandlerLoader = (ContentHandlerDbLoader) BbContext.instance()
						.getPersistenceManager().getLoader(ContentHandlerDbLoader.TYPE);
					contentHandler = contentHandlerLoader.loadByHandle(CONTENT_HANDLER_HANDLE);
					final Id basicLTIPlacementId = contentHandler.getBasicLTIPlacementId();
					BbUtil.trace("Loaded content handler from DB, placement = "
						+ (basicLTIPlacementId == null ? "null" : basicLTIPlacementId.toExternalString()));
				}
			}
		}
		return contentHandler;
	}

	public static String loadVersion() throws IOException
	{
		InputStream in = null;
		try
		{
			final Properties p = new Properties();
			in = Configuration.class.getResourceAsStream("/version.properties");
			p.load(in);
			final String mmr = p.getProperty("version.mmr");
			final String display = p.getProperty("version.display");
			return MessageFormat.format("{0} ({1})", mmr, display);
		}
		catch( Exception e )
		{
			BbUtil.error("Couldn't load version", e);
			throw Throwables.propagate(e);
		}
		finally
		{
			Closeables.close(in, false);
		}
	}

	public File getConfigDirectory()
	{
		return plugin.getConfigDirectory();
	}

	private String buildEquellaUrlFromDeprecatedConfigParams(Properties props) throws MalformedURLException
	{
		final boolean secure = Boolean.valueOf(props.getProperty(SECURE));
		final String host = props.getProperty(HOST, "localhost");
		final int port = Integer.parseInt(props.getProperty(PORT, "80"));
		final String context = props.getProperty(CONTEXT, "/");
		final String inst = props.getProperty(INSTITUTION, "");

		return new URL(new URL(secure ? "https" : "http", host, port, context), inst).toString();
	}

	public boolean hasBeenModified(Date lastUpdate)
	{
		return lastUpdate.before(lastModified);
	}

	private Set<String> commaSplit(/* @Nullable */String value)
	{
		final Set<String> result = new HashSet<String>();
		if( value != null )
		{
			final String[] vs = value.split(",");
			for( int i = 0; i < vs.length; i++ )
			{
				result.add(vs[i].trim());
			}
		}
		return result;
	}

	private String commaJoin(/* @Nullable */Collection<String> values)
	{
		final StringBuilder roles = new StringBuilder();
		if( values != null )
		{
			for( Iterator<String> iter = values.iterator(); iter.hasNext(); )
			{
				if( roles.length() > 0 )
				{
					roles.append(',');
				}
				roles.append(iter.next());
			}
		}
		return roles.toString();
	}

	/* @Nullable */
	public String getEquellaUrl()
	{
		return equellaUrl;
	}

	public void setEquellaUrl(/* @Nullable */String equellaUrl)
	{
		this.equellaUrl = (equellaUrl == null ? null : equellaUrl.trim());
		if( !Strings.isNullOrEmpty(this.equellaUrl) && !this.equellaUrl.endsWith("/") )
		{
			this.equellaUrl += '/';
		}
	}

	/* @Nullable */
	public String getSecret()
	{
		return secret;
	}

	public void setSecret(/* @Nullable */String secret)
	{
		if( secret == null || secret.length() == 0 )
		{
			this.secret = DEFAULT_SECRET;
		}
		else
		{
			this.secret = secret;
		}
	}

	/* @Nullable */
	public String getSecretId()
	{
		return secretid;
	}

	public void setSecretId(/* @Nullable */String secretid)
	{
		if( secretid == null || secretid.length() == 0 )
		{
			this.secretid = DEFAULT_SECRET;
		}
		else
		{
			this.secretid = secretid;
		}
	}

	/* @Nullable */
	public String getOauthClientId()
	{
		return oauthClientId;
	}

	public void setOauthClientId(/* @Nullable */String oauthClientId)
	{
		this.oauthClientId = oauthClientId;
	}

	/* @Nullable */
	public String getOauthClientSecret()
	{
		return oauthClientSecret;
	}

	public void setOauthClientSecret(/* @Nullable */String oauthClientSecret)
	{
		this.oauthClientSecret = oauthClientSecret;
	}

	/* @Nullable */
	public String getVersion()
	{
		return version;
	}

	public void setMockPortalRoles(/* @Nullable */Set<String> mockPortalRoles)
	{
		this.mockPortalRoles = mockPortalRoles;
	}

	/* @Nullable */
	public Set<String> getMockPortalRoles()
	{
		return mockPortalRoles;
	}

	/* @Nullable */
	public String getRestriction()
	{
		return restriction;
	}

	public void setRestriction(/* @Nullable */String restriction)
	{
		if( restriction == null )
		{
			this.restriction = DEFAULT_RESTRICTION;
		}
		else
		{
			this.restriction = restriction;
		}
	}

	public void setNewWindow(boolean newWindow)
	{
		this.newWindow = newWindow;
	}

	public boolean isNewWindow()
	{
		return newWindow;
	}
}