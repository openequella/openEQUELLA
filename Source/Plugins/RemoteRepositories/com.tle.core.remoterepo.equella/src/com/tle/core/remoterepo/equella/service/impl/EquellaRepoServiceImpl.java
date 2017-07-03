/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.remoterepo.equella.service.impl;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.factory.AbstractServiceConfiguration;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.dytech.devlib.PropBagEx;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tle.beans.search.TLESettings;
import com.tle.common.beans.UploadCallbackInputStream;
import com.tle.common.beans.progress.PercentageProgressCallback;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.util.TokenGenerator;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.guice.Bind;
import com.tle.core.harvester.soap.SoapHarvesterService;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.remoterepo.equella.service.EquellaRepoService;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.TaskService;
import com.tle.core.services.impl.BeanClusteredTask;
import com.tle.core.services.impl.SingleShotTask;
import com.tle.core.services.impl.Task;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.util.archive.ArchiveType;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind(EquellaRepoService.class)
@Singleton
public class EquellaRepoServiceImpl implements EquellaRepoService
{
	private static final Logger LOGGER = Logger.getLogger(EquellaRepoService.class);

	private static final String HARVESTER_ENDPOINT = "services/SoapHarvesterService";
	// This is not ideal...
	private static final Set<String> FILE_BASED_ATTACHMENT_TYPES = new HashSet<String>(
		Arrays.asList(new String[]{"local", "zip", "imsres", "html"}));

	private final Cache<String, AttachmentDownloadSessionImpl> sessions = CacheBuilder.newBuilder().softValues()
		.expireAfterAccess(30, TimeUnit.MINUTES).build();

	@Inject
	private StagingService stagingService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private TaskService taskService;
	@Inject
	private RunAsInstitution runAs;

	@Override
	public String downloadAttachments(TLESettings settings, String uuid, int version,
		PercentageProgressCallback callback)
	{
		try
		{
			final String institutionUrl = settings.getInstitutionUrl();
			final String key = Integer.toString((institutionUrl + uuid + version).hashCode());
			AttachmentDownloadSessionImpl session = sessions.getIfPresent(key);
			// no current session, OR the last one blew up, OR the session is
			// finished and the files are no longer there
			if( session == null || session.getError() != null
				|| (session.isFinished() && !fileSystemService.fileExists(session.getStaging())) )
			{
				session = new AttachmentDownloadSessionImpl();
				session.setId(key);
				session.setFinished(false);
				session.setCallback(callback);
				sessions.put(session.getId(), session);

				final String username = (settings.isUseLoggedInUser() ? CurrentUser.getUsername()
					: settings.getUsername());
				taskService.getGlobalTask(
					new BeanClusteredTask(key, EquellaRepoService.class, "createDownloadTask",
						CurrentUser.getUserState(), username, uuid, version, settings.getInstitutionUrl(),
						settings.getSharedSecretId(), settings.getSharedSecretValue(), key),
					TimeUnit.MINUTES.toMillis(1));
				return key;
			}

			callback.setTotalSize(1);
			callback.setBytesRead(1);
			callback.setFinished();
			return key;
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public Task createDownloadTask(UserState userState, String username, String uuid, int version, String url,
		String sharedId, String sharedValue, String key)
	{
		return new DownloadTask(userState, username, uuid, version, url, sharedId, sharedValue, key);
	}

	@Override
	public AttachmentDownloadSession downloadProgress(String sessionId)
	{
		// No session id if no attachments were downloaded
		if( sessionId == null )
		{
			final AttachmentDownloadSessionImpl session = new AttachmentDownloadSessionImpl();
			session.setFinished(true);
			return session;
		}

		final AttachmentDownloadSessionImpl session = sessions.getIfPresent(sessionId);
		if( session == null )
		{
			throw new RuntimeException("Download session " + sessionId + " does not exist");
		}
		final Exception e = session.getError();
		if( e != null )
		{
			Throwables.propagate(e);
		}
		return session;
	}

	@Override
	public StagingFile getDownloadedFiles(AttachmentDownloadSession session)
	{
		AttachmentDownloadSessionImpl s = (AttachmentDownloadSessionImpl) session;
		return s.isFinished() ? s.getStaging() : null;
	}

	@Override
	public PropBagEx getItemXml(TLESettings settings, String uuid, int version, boolean stripFileBasedAttachments)
	{
		try
		{
			final String username = (settings.isUseLoggedInUser() ? CurrentUser.getUsername() : settings.getUsername());
			final SoapHarvesterService client = getNewClient(settings.getInstitutionUrl(), settings.getSharedSecretId(),
				settings.getSharedSecretValue(), username);
			final PropBagEx xml = new PropBagEx(client.getItemXml(uuid, version));
			client.logout();
			if( stripFileBasedAttachments )
			{
				for( Node n : xml.iterateAllNodes("item/attachments/attachment") )
				{
					Node type = n.getAttributes().getNamedItem("type");
					if( type != null && FILE_BASED_ATTACHMENT_TYPES.contains(type.getTextContent()) )
					{
						n.getParentNode().removeChild(n);
					}
				}
			}
			return xml;
		}
		catch( Exception e )
		{
			Throwables.propagate(e);
			return null; // not possible
		}
	}

	protected SoapHarvesterService getNewClient(String url, String sharedId, String sharedValue, String username)
	{
		try
		{
			final URL endpointUrl = new URL(new URL(url), HARVESTER_ENDPOINT);

			ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
			Bus bus = new ExtensionManagerBus(null, null, Bus.class.getClassLoader());
			factory.setBus(bus);
			factory.setServiceClass(SoapHarvesterService.class);
			factory.setServiceName(
				new QName("http://soap.harvester.core.tle.com", SoapHarvesterService.class.getSimpleName()));
			factory.setAddress(endpointUrl.toString());
			factory.setDataBinding(new AegisDatabinding());
			List<AbstractServiceConfiguration> configs = factory.getServiceFactory().getServiceConfigurations();
			configs.add(0, new XFireReturnTypeConfig());
			SoapHarvesterService soapClient = (SoapHarvesterService) factory.create();
			Client client = ClientProxy.getClient(soapClient);
			client.getRequestContext().put(Message.MAINTAIN_SESSION, true);
			HTTPClientPolicy policy = new HTTPClientPolicy();
			policy.setReceiveTimeout(600000);
			policy.setAllowChunking(false);
			HTTPConduit conduit = (HTTPConduit) client.getConduit();
			// Works?
			// conduit.getTlsClientParameters().setSSLSocketFactory(BlindSSLSocketFactory.getDefaultSSL());
			conduit.setClient(policy);

			soapClient.loginWithToken(TokenGenerator.createSecureToken(username, sharedId, sharedValue, null));
			return soapClient;
		}
		catch( Exception x )
		{
			LOGGER.error("Error connecting to remote EQUELLA server", x);
			throw new RuntimeException(
				CurrentLocale.get("com.tle.core.remoterepo.equella.error.communication", x.getMessage()));
		}
	}

	private class DownloadTask extends SingleShotTask
	{
		private final String username;
		private final String uuid;
		private final int version;
		private final String url;
		private final String sharedId;
		private final String sharedValue;
		private final UserState userState;
		private final String key;

		public DownloadTask(UserState userState, String username, String uuid, int version, String url, String sharedId,
			String sharedValue, String key)
		{
			this.username = username;
			this.uuid = uuid;
			this.version = version;
			this.url = url;
			this.sharedId = sharedId;
			this.sharedValue = sharedValue;
			this.userState = userState;
			this.key = key;
		}

		public void doDownload()
		{
			try
			{
				SoapHarvesterService client = getNewClient(url, sharedId, sharedValue, username);
				try
				{
					final URL downloadUrl = new URL(client.prepareDownload(uuid, version));
					HttpURLConnection conn = (HttpURLConnection) downloadUrl.openConnection();
					StagingFile staging = stagingService.createStagingArea();
					AttachmentDownloadSessionImpl session = sessions.getIfPresent(key);
					session.setStaging(staging);
					PercentageProgressCallback callback = session.getCallback();
					callback.setTotalSize(conn.getContentLength());
					UploadCallbackInputStream inputStream = new UploadCallbackInputStream(conn.getInputStream(),
						callback);
					fileSystemService.unzipFile(staging, inputStream, ArchiveType.TAR_GZ);
					session.setFinished(true);
				}
				finally
				{
					try
					{
						client.logout();
					}
					catch( Exception e )
					{
						// Ignore
					}
				}
			}
			catch( Exception e )
			{
				sessions.getIfPresent(key).setError(e);
				LOGGER.error("Error downloading item", e);
			}
		}

		@Override
		public void runTask()
		{
			runAs.execute(userState, new Callable<Void>()
			{
				@Override
				public Void call() throws Exception
				{
					doDownload();
					return null;
				}
			});

		}

		@Override
		protected String getTitleKey()
		{
			return null;
		}
	}

	public static class AttachmentDownloadSessionImpl implements AttachmentDownloadSession
	{
		private String id;
		private StagingFile staging;
		private boolean finished;
		private Exception error;
		private PercentageProgressCallback callback;

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public StagingFile getStaging()
		{
			return staging;
		}

		public void setStaging(StagingFile staging)
		{
			this.staging = staging;
		}

		@Override
		public boolean isFinished()
		{
			return finished;
		}

		public void setFinished(boolean finished)
		{
			this.finished = finished;
			if( finished && callback != null )
			{
				callback.setFinished();
			}
		}

		public Exception getError()
		{
			return error;
		}

		public void setError(Exception error)
		{
			this.error = error;
			callback.setErrorMessage(error.getMessage());
		}

		public PercentageProgressCallback getCallback()
		{
			return callback;
		}

		public void setCallback(PercentageProgressCallback callback)
		{
			this.callback = callback;
		}
	}

	private static class XFireReturnTypeConfig extends AbstractServiceConfiguration
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
