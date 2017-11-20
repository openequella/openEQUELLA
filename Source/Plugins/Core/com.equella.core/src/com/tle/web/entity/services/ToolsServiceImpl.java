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

package com.tle.web.entity.services;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemPack;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.activecache.RemoteCachingService;
import com.tle.core.guice.Bind;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.helper.ItemHelper.ItemHelperSettings;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.remoting.ToolsService;

@Bind
@Singleton
public class ToolsServiceImpl extends AbstractSoapService implements ToolsService
{
	@Inject
	private RemoteCachingService remoteCachingService;
	@Inject
	private ItemService itemService;
	@Inject
	private ItemHelper itemHelper;
	@Inject
	private ItemOperationFactory workflowFactory;

	@Override
	public String[] getCacheList(String ssid, String lastUpdate)
	{
		try
		{
			authenticate(ssid);
			final List<String> l = remoteCachingService.getCacheList(lastUpdate);
			return l.toArray(new String[l.size()]);
		}
		catch( final Exception ex )
		{
			throw new RuntimeException(ex);
		}
	}

	@Override
	public String getCacheSchedule(String ssid)
	{
		try
		{
			authenticate(ssid);
			// Right away!
			return new UtcDate().format(Dates.ISO_WITH_TIMEZONE);
		}
		catch( final Exception ex )
		{
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void archive(String session, String uuid, int version, String itemdef)
	{
		try
		{
			authenticate(session);
			ItemId key = new ItemId(uuid, version);
			itemService.operation(key, new WorkflowOperation[]{workflowFactory.archive()});
		}
		catch( final Exception ex )
		{
			throw new RuntimeException(ex);
		}
	}

	@Override
	public String newVersion(String session, String uuid, int version, boolean copyAttachments)
	{
		try
		{
			authenticate(session);

			ItemId key = new ItemId(uuid, version);
			ItemPack pack = itemService.operation(key, workflowFactory.newVersion(copyAttachments));

			return itemHelper.convertToXml(pack, new ItemHelperSettings(false)).toString();
		}
		catch( final Exception ex )
		{
			throw new RuntimeException(ex);
		}
	}

	@Override
	public String getUserId(String session)
	{
		return CurrentUser.getUserID();
	}
}
