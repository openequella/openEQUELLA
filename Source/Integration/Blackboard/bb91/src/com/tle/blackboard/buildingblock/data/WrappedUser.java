package com.tle.blackboard.buildingblock.data;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.factory.AbstractServiceConfiguration;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import blackboard.data.course.CourseMembership;
import blackboard.data.user.User;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.user.UserDbLoader;
import blackboard.platform.BbServiceManager;
import blackboard.platform.context.Context;
import blackboard.platform.context.ContextManager;
import blackboard.platform.context.ContextManagerFactory;
import blackboard.platform.session.BbSession;
import blackboard.platform.session.BbSessionManagerService;
import blackboard.platform.session.BbSessionManagerServiceFactory;

import com.google.common.base.Throwables;
import com.tle.blackboard.buildingblock.Configuration;
import com.tle.blackboard.buildingblock.TokenGenerator;
import com.tle.blackboard.common.BbContext;
import com.tle.blackboard.common.BbUtil;
import com.tle.blackboard.common.propbag.PropBagMin;
import com.tle.blackboard.common.propbag.PropBagMin.PropBagMinThoroughIterator;
import com.tle.web.remoting.soap.SoapService51;

@SuppressWarnings("nls")
// @NonNullByDefault
public class WrappedUser
{
	private int releases = 0;
	private long taskUpdate;

	/* @Nullable */
	private String session_id;
	/* @Nullable */
	private String username;
	/* @Nullable */
	private Context ctx;
	/* @Nullable */
	private BbSession session;
	/* @Nullable */
	private ContextManager contextManager;
	/* @Nullable */
	private User bbuser;
	/* @Nullable */
	private List<TaskLink> taskLinks;
	/* @Nullable */
	private UserDbLoader userDbLoader;

	@SuppressWarnings("null")
	public String getToken()
	{
		try
		{
			Configuration config = Configuration.instance();
			return TokenGenerator.createSecureToken(getUsername(), config.getSecretId(), config.getSecret(),
				getSessionId());
		}
		catch( IOException e )
		{
			BbUtil.error("Error generating token", e);
			throw Throwables.propagate(e);
		}
	}

	private synchronized SoapService51 getSoapService() throws Exception
	{
		final URL endpointUrl = new URL(new URL(Configuration.instance().getEquellaUrl()), "services/SoapService51");

		final ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
		factory.setServiceClass(SoapService51.class);
		factory.setServiceName(new QName("http://soap.remoting.web.tle.com", SoapService51.class.getSimpleName()));
		factory.setAddress(endpointUrl.toString());
		final List<AbstractServiceConfiguration> configs = factory.getServiceFactory().getServiceConfigurations();
		configs.add(0, new XFireReturnTypeConfig());
		factory.setDataBinding(new AegisDatabinding());

		final SoapService51 soapClient = (SoapService51) factory.create();
		final Client client = ClientProxy.getClient(soapClient);
		client.getRequestContext().put(Message.MAINTAIN_SESSION, true);
		final HTTPClientPolicy policy = new HTTPClientPolicy();
		policy.setReceiveTimeout(600000);
		policy.setAllowChunking(false);
		final HTTPConduit conduit = (HTTPConduit) client.getConduit();
		final TLSClientParameters tls = new TLSClientParameters();
		tls.setSSLSocketFactory(createBlindSSLContext().getSocketFactory());
		conduit.setTlsClientParameters(tls);
		conduit.setClient(policy);

		soapClient.loginWithToken(getToken());

		return soapClient;
	}

	private static SSLContext createBlindSSLContext()
	{
		// Create a trust manager that will purposefully fall down on the job
		TrustManager[] blindTrustMan = new TrustManager[]{new X509TrustManager()
		{
			@Override
			public X509Certificate[] getAcceptedIssuers()
			{
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] c, String a)
			{
				// Ignore
			}

			@Override
			public void checkServerTrusted(X509Certificate[] c, String a)
			{
				// IGNORE
			}
		}};

		// create our "blind" ssl socket factory with our lazy trust manager
		try
		{
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, blindTrustMan, new SecureRandom());
			return sc;
		}
		catch( GeneralSecurityException ex )
		{
			throw new RuntimeException("Error creating blind SSL context", ex);
		}
	}

	@SuppressWarnings("null")
	public List<TaskLink> getTaskLinks() throws Exception
	{
		if( taskLinks == null || taskUpdate < System.currentTimeMillis() )
		{
			final List<TaskLink> links = new ArrayList<TaskLink>();
			final PropBagMin filterCounts = new PropBagMin(getSoapService().getTaskFilterCounts(true));
			final PropBagMinThoroughIterator iter = filterCounts.iterateAll("filter");
			while( iter.hasNext() )
			{
				final PropBagMin filter = iter.next();
				final String text = filter.getNode("name") + " (" + filter.getNode("count") + ")";
				final String href = filter.getNode("href");
				links.add(new TaskLink(text, href));
			}
			taskLinks = links;
			taskUpdate = System.currentTimeMillis() + (1000 * 60 * 5);
		}
		return taskLinks;
	}

	public CourseMembership getCourseMembership(Id courseId) throws Exception
	{
		final CourseMembershipDbLoader membershipLoader = (CourseMembershipDbLoader) BbContext.instance()
			.getPersistenceManager().getLoader(CourseMembershipDbLoader.TYPE);
		return getCourseMembership(membershipLoader, courseId);
	}

	public CourseMembership getCourseMembership(CourseMembershipDbLoader membershipLoader, Id courseId)
		throws Exception
	{
		return membershipLoader.loadByCourseAndUserId(courseId, getId());
	}

	@SuppressWarnings("null")
	private void setContext(HttpServletRequest request)
	{
		final String usernameParam = request.getParameter("username");
		if( usernameParam != null )
		{
			this.username = usernameParam;
		}

		try
		{
			if( contextManager == null )
			{
				contextManager = (ContextManager) BbServiceManager.lookupService(ContextManager.class);
			}
			// Will throw an error if not set...
			contextManager.getContext();
		}
		catch( Exception e )
		{
			try
			{
				// DO NOT SET THE CONTEXT UNLESS YOU HAVEN'T GOT ONE
				// OTHERWISE BLACKBOARD WILL THROW ERRORS ON LOGIN
				// YOU HAVE BEEN WARNED!!!
				contextManager.setContext(request);
				releases++;
			}
			catch( Exception ex )
			{
				BbUtil.error("Error setting context", ex);
			}
		}

		final BbSessionManagerService sessionService = BbSessionManagerServiceFactory.getInstance();
		session = sessionService.getSession(request);
		ctx = ContextManagerFactory.getInstance().getContext();
		try
		{
			if( username == null )
			{
				username = getUser().getUserName();
			}
		}
		catch( Exception e )
		{
			// IGNORE
		}
	}

	public void clearContext()
	{
		try
		{
			if( contextManager != null )
			{
				while( releases > 0 )
				{
					contextManager.releaseContext();
					releases--;
				}
			}
		}
		catch( Exception e )
		{
			BbUtil.error("Couldn't release context for user " + username + " " + getSessionId(), e);
		}
	}

	public String getUsername()
	{
		return getUser().getUserName();
	}

	public String getFamilyName()
	{
		return getUser().getFamilyName();
	}

	public String getGivenName()
	{
		return getUser().getGivenName();
	}

	@SuppressWarnings("null")
	public String getSessionId()
	{
		if( session == null )
		{
			throw new RuntimeException("Session not initialised");
		}
		final String sess_id = session.getBbSessionIdMd5();
		if( !sess_id.equals("anonymous-session-default-md5") || session_id == null )
		{
			session_id = sess_id;
		}
		return session_id;
	}

	public User getUser()
	{
		try
		{
			if( bbuser != null )
			{
				return bbuser;
			}
			else if( ctx != null )
			{
				return ctx.getUser();
			}
			else
			{
				return getUserLoader().loadByUserName(username);
			}
		}
		catch( Exception e )
		{
			BbUtil.error("Error in getUser()", e);
			throw Throwables.propagate(e);
		}
	}

	public Id getId()
	{
		return getUser().getId();
	}

	public BbSession getSession()
	{
		return session;
	}

	@SuppressWarnings("null")
	private UserDbLoader getUserLoader()
	{
		if( userDbLoader == null )
		{
			try
			{
				this.userDbLoader = (UserDbLoader) BbContext.instance().getPersistenceManager()
					.getLoader(UserDbLoader.TYPE);
			}
			catch( final PersistenceException e )
			{
				throw Throwables.propagate(e);
			}
		}
		return userDbLoader;
	}

	public static WrappedUser getUser(HttpServletRequest request)
	{
		final WrappedUser user = new WrappedUser();
		user.setContext(request);
		WrappedUser suser = null;
		try
		{
			suser = (WrappedUser) request.getSession().getAttribute("TLE_USER");

			// Have probably been logged out...
			if( suser != null && !user.getId().equals(suser.getId()) )
			{
				suser = null;
			}
		}
		catch( final Exception cce )
		{
			suser = null;
			// When BB gets redeployed this will happen
		}

		if( suser == null )
		{
			suser = user;
			request.getSession().setAttribute("TLE_USER", user);
		}
		return suser;
	}

	public static class TaskLink
	{
		private final String href;
		private final String text;

		public TaskLink(String text, String href)
		{
			this.text = text;
			this.href = href;
		}

		public String getHref()
		{
			return href;
		}

		public String getText()
		{
			return text;
		}
	}

	public static class XFireReturnTypeConfig extends AbstractServiceConfiguration
	{
		@Override
		public QName getInParameterName(OperationInfo op, Method method, int paramNumber)
		{
			return new QName(op.getName().getNamespaceURI(), "in" + paramNumber);
		}

		@Override
		public QName getOutParameterName(OperationInfo op, Method method, int paramNumber)
		{
			return new QName(op.getName().getNamespaceURI(), "out");
		}
	}
}