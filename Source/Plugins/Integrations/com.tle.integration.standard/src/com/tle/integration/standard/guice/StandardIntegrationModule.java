package com.tle.integration.standard.guice;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.tle.integration.standard.GenericIntegDownloadSection;

@SuppressWarnings("nls")
public class StandardIntegrationModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("com.tle.integration.standard.IntegrationPackDownload")).to(
			IntegrationPackDownload.class);
		bind(Object.class).annotatedWith(Names.named("com.tle.integration.standard.ActiveCacheToolDownload")).to(
			ActiveCacheToolDownload.class);
		bind(Object.class).annotatedWith(Names.named("com.tle.integration.standard.HTMLPluginsDownload")).to(
			HTMLPluginsDownload.class);
	}

	public static class IntegrationPackDownload extends GenericIntegDownloadSection
	{
		public IntegrationPackDownload()
		{
			setIntegrationType("integpack");
			setDownloadFile("equella-integration-pack.zip");
		}
	}

	public static class ActiveCacheToolDownload extends GenericIntegDownloadSection
	{
		public ActiveCacheToolDownload()
		{
			setIntegrationType("activecachetool");
			setDownloadFile("equella-activecache.zip");
		}
	}

	public static class HTMLPluginsDownload extends GenericIntegDownloadSection
	{
		public HTMLPluginsDownload()
		{
			setIntegrationType("htmleditorplugins");
			setDownloadFile("equella-htmleditorplugins.zip");
		}
	}
}
