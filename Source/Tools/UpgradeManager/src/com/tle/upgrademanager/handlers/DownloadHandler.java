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

package com.tle.upgrademanager.handlers;

import java.io.IOException;
import java.util.UUID;

import com.sun.net.httpserver.HttpExchange;
import com.tle.upgrademanager.ManagerConfig;
import com.tle.upgrademanager.handlers.PagesHandler.WebVersion;
import com.tle.upgrademanager.helpers.AjaxState;
import com.tle.upgrademanager.helpers.Upgrader;
import com.tle.upgrademanager.helpers.Version;

@SuppressWarnings("nls")
public class DownloadHandler extends UrlDispatchHandler
{
	private final ManagerConfig config;
	private final AjaxState ajax;

	public DownloadHandler(ManagerConfig config, AjaxState ajax)
	{
		this.config = config;
		this.ajax = ajax;
	}

	public void download(HttpExchange exchange) throws IOException
	{
		final String ajaxId = UUID.randomUUID().toString();

		new Thread()
		{
			@Override
			public void run()
			{
				Upgrader upgrader = new Upgrader(ajaxId, ajax, config);
				WebVersion oldVersion;
				WebVersion newVersion;
				try
				{
					Version version = new Version(config);
					newVersion = version.getLatestVersion(version.getVersions().first());
					oldVersion = version.getVersions().first();

					upgrader.downloadUpgrade(oldVersion.getFilename(), newVersion.getFilename());
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		}.start();

		HttpExchangeUtils.respondRedirect(exchange, "/pages/progress/" + ajaxId);
	}
}
