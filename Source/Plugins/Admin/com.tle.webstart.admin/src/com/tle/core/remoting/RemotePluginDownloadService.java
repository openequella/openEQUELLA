package com.tle.core.remoting;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

public interface RemotePluginDownloadService
{
	List<PluginDetails> getAllPluginDetails(String pluginType);

	class PluginDetails implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final URL baseUrl;
		private final String manifestXml;

		public PluginDetails(URL baseUrl, String manifestXml)
		{
			this.baseUrl = baseUrl;
			this.manifestXml = manifestXml;
		}

		public URL getBaseUrl()
		{
			return baseUrl;
		}

		public String getManifestXml()
		{
			return manifestXml;
		}
	}
}
