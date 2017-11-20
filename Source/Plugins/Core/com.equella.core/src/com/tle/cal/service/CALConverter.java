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

package com.tle.cal.service;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.exceptions.WorkflowException;
import com.google.inject.Provider;
import com.tle.beans.Institution;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.cal.CALConstants;
import com.tle.common.NameValue;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.DefaultMessageCallback;
import com.tle.core.institution.convert.service.InstitutionImportService.ConvertType;
import com.tle.core.institution.convert.service.impl.InstitutionImportServiceImpl.ConverterTasks;
import com.tle.core.item.operations.BaseFilter;
import com.tle.core.item.operations.FilterResultListener;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class CALConverter extends AbstractConverter<Object>
{
	private static final String CAL_CID = "cal_converter"; //$NON-NLS-1$

	private static String KPFX = PluginServiceImpl.getMyPluginId(CALConverter.class) + "."; //$NON-NLS-1$

	@Inject
	private ItemService itemService;
	@Inject
	private Provider<CALCollectFilter> filterFactory;

	@Override
	public void doDelete(Institution institution, ConverterParams callback)
	{
		// nothing
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams callback)
		throws IOException
	{
		// nothing
	}

	@Override
	public void importIt(TemporaryFileHandle staging, Institution institution, ConverterParams params, String cid)
		throws IOException
	{
		try
		{
			final DefaultMessageCallback message = new DefaultMessageCallback(null);
			params.setMessageCallback(message);
			message.setKey(KPFX + "calculatemessage"); //$NON-NLS-1$
			itemService.operateAll(filterFactory.get(), new FilterResultListener()
			{
				@Override
				public void failed(ItemKey itemId, Item item, ItemPack pack, Throwable e)
				{
					message.incrementCurrent();
				}

				@Override
				public void succeeded(ItemKey itemId, ItemPack pack)
				{
					message.incrementCurrent();
				}

				@Override
				public void total(int total)
				{
					message.setKey(KPFX + "cal.statusmsg"); //$NON-NLS-1$
					message.setTotal(total);
				}
			});
		}
		catch( WorkflowException e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void addTasks(ConvertType type, ConverterTasks tasks, ConverterParams params)
	{
		if( !params.hasFlag(ConverterParams.NO_ITEMS) )
		{
			if( type == ConvertType.IMPORT || type == ConvertType.CLONE )
			{
				tasks.addAfter(new NameValue(CurrentLocale.get(KPFX + "cal.collecttask"), CAL_CID)); //$NON-NLS-1$
			}
		}
	}

	@Override
	public ConverterId getConverterId()
	{
		return null;
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		// dummy
	}

	@Bind
	public static class CALCollectFilter extends BaseFilter
	{
		@Inject
		private Provider<CALCollectOperation> collectFactory;

		@Override
		protected WorkflowOperation[] createOperations()
		{
			return new WorkflowOperation[]{collectFactory.get()};
		}

		@Override
		public String getJoinClause()
		{
			return "join i.itemDefinition d left join d.attributes a with a.key = '" + CALConstants.ENABLED + "' "; //$NON-NLS-1$//$NON-NLS-2$
		}

		@Override
		public String getWhereClause()
		{
			return "a.value = 'true'"; //$NON-NLS-1$
		}
	}

}
