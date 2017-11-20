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

package com.tle.web.itemadmin.operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tle.beans.item.ItemPack;
import com.tle.core.guice.Bind;
import com.tle.core.item.standard.operations.DeleteOperation;
import com.tle.core.item.standard.operations.PurgeOperation;
import com.tle.core.item.standard.operations.ReactivateOperation;
import com.tle.core.item.standard.operations.RestoreDeletedOperation;
import com.tle.core.item.standard.operations.ResumeOperation;
import com.tle.core.item.standard.operations.workflow.ArchiveOperation;
import com.tle.core.item.standard.operations.workflow.RedraftOperation;
import com.tle.core.item.standard.operations.workflow.ResetOperation;
import com.tle.core.item.standard.operations.workflow.ReviewOperation;
import com.tle.core.item.standard.operations.workflow.SubmitOperation;
import com.tle.core.item.standard.operations.workflow.SuspendOperation;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.plugins.SerializedBeanLocator;
import com.tle.web.bulk.operation.BulkOperationExtension;
import com.tle.web.bulk.operation.SimpleOperationExecutor;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.model.Option;

@Bind
public class StandardOperations extends AbstractPrototypeSection<Object> implements BulkOperationExtension
{
	static
	{
		PluginResourceHandler.init(StandardOperations.class);
	}

	@PlugKey("operation.")
	private static String KEY_NAME;
	@PlugKey("itemadmin.opresults.status")
	private static String KEY_STATUS;

	public static String getNameKey()
	{
		return KEY_NAME;
	}

	public static String getStatusKey()
	{
		return KEY_STATUS;
	}

	private final List<StandardOperation> opList = new ArrayList<StandardOperation>();
	private final Map<String, StandardOperation> opMap = new HashMap<String, StandardOperation>();

	@Override
	public BeanLocator<SimpleOperationExecutor> getExecutor(SectionInfo info, String operationId)
	{
		return new SerializedBeanLocator<SimpleOperationExecutor>(opMap.get(operationId).getExecutor());
	}

	@Override
	public void addOptions(SectionInfo info, List<Option<OperationInfo>> opsList)
	{
		for( StandardOperation op : opList )
		{
			final String operationId = op.getOperationId();
			opsList.add(new KeyOption<OperationInfo>(KEY_NAME + operationId, operationId,
				new OperationInfo(this, operationId)));
		}
	}

	@SuppressWarnings("nls")
	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		opList.add(new StandardOperation("redraft", RedraftOperation.class, "redraft"));
		opList.add(new StandardOperation("submit", SubmitOperation.class, "submit"));
		opList.add(new StandardOperation("delete", DeleteOperation.class, "delete"));
		opList.add(new StandardOperation("purge", PurgeOperation.class, "purge", false, true));
		opList.add(new StandardOperation("makelive", ReactivateOperation.class, "reactivate"));
		opList.add(new StandardOperation("reset", ResetOperation.class, "reset"));
		opList.add(new StandardOperation("restore", RestoreDeletedOperation.class, "restore"));
		opList.add(new StandardOperation("archive", ArchiveOperation.class, "archive"));
		opList.add(new StandardOperation("suspend", SuspendOperation.class, "suspend"));
		opList.add(new StandardOperation("resume", ResumeOperation.class, "resume"));
		opList.add(new StandardOperation("review", ReviewOperation.class, "review"));
		for( StandardOperation op : opList )
		{
			opMap.put(op.getOperationId(), op);
		}
	}

	@SuppressWarnings("nls")
	@Override
	public Label getStatusTitleLabel(SectionInfo info, String operationId)
	{
		return new KeyLabel(KEY_STATUS, new KeyLabel(KEY_NAME + operationId + ".title"));
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
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
	public SectionRenderable renderOptions(RenderContext context, String operationId)
	{
		return null;
	}

	@Override
	public void prepareDefaultOptions(SectionInfo info, String operationId)
	{
		// nothing
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
