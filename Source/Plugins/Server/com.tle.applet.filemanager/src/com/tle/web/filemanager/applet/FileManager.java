package com.tle.web.filemanager.applet;

import java.net.URL;
import java.util.logging.Logger;

import javax.swing.JComponent;

import com.dytech.edge.common.Version;
import com.tle.web.filemanager.applet.backend.AbstractRemoteBackendImpl;
import com.tle.web.filemanager.applet.backend.Backend;
import com.tle.web.filemanager.applet.backend.CachingBackend;

public class FileManager
{
	private static final Logger LOGGER = Logger.getLogger(FileManager.class.getName());

	private final MainPanel mainPanel;

	@SuppressWarnings("nls")
	public FileManager(Parameters params) throws Exception
	{
		LOGGER.info("Applet version is " + Version.load().getFull());

		LOGGER.info("Wizard ID is " + params.getWizardId());
		LOGGER.info("Backend class is " + params.getBackendClass().getName());

		LOGGER.info("Initialise backend...");
		Backend backend = getBackend(params);

		LOGGER.info("Starting applet...");
		mainPanel = new MainPanel(backend, params);
	}

	public JComponent getComponent()
	{
		return mainPanel;
	}

	private Backend getBackend(Parameters params) throws Exception
	{

		AbstractRemoteBackendImpl backend = params.getBackendClass().getConstructor(URL.class, String.class)
			.newInstance(params.getServerUrl(), params.getWizardId());

		return new CachingBackend(backend);
	}

	public static class Parameters
	{
		private final URL serverUrl;
		private final Class<? extends AbstractRemoteBackendImpl> backendClass;
		private final String wizardId;
		private final boolean autoMarkAsResource;

		public Parameters(URL serverUrl, Class<? extends AbstractRemoteBackendImpl> backendClass, String wizardId,
			boolean autoMarkAsResource)
		{
			this.serverUrl = serverUrl;
			this.backendClass = backendClass;
			this.wizardId = wizardId;
			this.autoMarkAsResource = autoMarkAsResource;
		}

		public URL getServerUrl()
		{
			return serverUrl;
		}

		public Class<? extends AbstractRemoteBackendImpl> getBackendClass()
		{
			return backendClass;
		}

		public String getWizardId()
		{
			return wizardId;
		}

		public boolean isAutoMarkAsResource()
		{
			return autoMarkAsResource;
		}
	}
}
