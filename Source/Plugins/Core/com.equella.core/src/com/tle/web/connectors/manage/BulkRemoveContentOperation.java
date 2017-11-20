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

package com.tle.web.connectors.manage;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.ItemPack;
import com.tle.core.connectors.service.ConnectorOperationFactory;
import com.tle.core.guice.Bind;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.plugins.ClassBeanLocator;
import com.tle.web.bulk.operation.BulkOperationExecutor;
import com.tle.web.bulk.operation.BulkOperationExtension;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.model.Option;

@Bind
@Singleton
public class BulkRemoveContentOperation implements BulkOperationExtension
{
	static
	{
		PluginResourceHandler.init(BulkRemoveContentOperation.class);
	}

	@Bind
	public static class RemoveContentOperationExecutor implements BulkOperationExecutor
	{
		@Inject
		private ConnectorOperationFactory operationFactory;

		@Override
		public WorkflowOperation[] getOperations()
		{
			return new WorkflowOperation[]{operationFactory.createDelete()};
		}

		@Override
		public String getTitleKey()
		{

			return "com.tle.web.connectors.bulk.removecontent.title";
		}
	}

	@PlugKey("operation.remove")
	private static String KEY_NAME;
	@PlugKey("connector.opresults.status")
	private static String KEY_STATUS;

	private static final String BULK_VALUE = "remove"; //$NON-NLS-1$

	@Override
	public void register(SectionTree tree, String parentId)
	{

	}

	@Override
	public void addOptions(SectionInfo info, List<Option<OperationInfo>> options)
	{

		options.add(new KeyOption<OperationInfo>(KEY_NAME, BULK_VALUE, new OperationInfo(this, BULK_VALUE)));
	}

	@Override
	public BeanLocator<? extends BulkOperationExecutor> getExecutor(SectionInfo info, String operationId)
	{
		return new ClassBeanLocator<BulkOperationExecutor>(RemoveContentOperationExecutor.class);
	}

	@Override
	public void prepareDefaultOptions(SectionInfo info, String operationId)
	{

	}

	@Override
	public SectionRenderable renderOptions(RenderContext context, String operationId)
	{
		return null;
	}

	@Override
	public Label getStatusTitleLabel(SectionInfo info, String operationId)
	{
		return new KeyLabel(KEY_STATUS, new KeyLabel(KEY_NAME + ".title")); //$NON-NLS-1$
	}

	@Override
	public boolean validateOptions(SectionInfo info, String operationId)
	{
		return true;
	}

	@Override
	public boolean areOptionsFinished(SectionInfo info, String operationId)
	{
		return true;
	}

	@Override
	public boolean hasExtraOptions(SectionInfo info, String operationId)
	{
		return false;
	}

	@Override
	public boolean hasExtraNavigation(SectionInfo info, String operationId)
	{
		return false;
	}

	@Override
	public Collection<Button> getExtraNavigation(SectionInfo info, String operationId)
	{
		return null;
	}

	@Override
	public boolean hasPreview(SectionInfo info, String operationId)
	{
		return false;
	}

	@Override
	public ItemPack runPreview(SectionInfo info, String operationId, long itemUuid)
	{
		return null;
	}

	@Override
	public boolean showPreviousButton(SectionInfo info, String opererationId)
	{
		return true;
	}

}
