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
