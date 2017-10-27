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

package com.tle.web.bulk.workflowtask.dialog;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.assistedinject.Assisted;
import com.tle.beans.item.ItemPack;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.guice.BindFactory;
import com.tle.core.institution.AclService;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.core.security.TLEAclManager;
import com.tle.web.bulk.operation.BulkOperationExecutor;
import com.tle.web.bulk.operation.BulkOperationExtension;
import com.tle.web.bulk.workflowtask.BulkWorkflowTaskOperationFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.AbstractSelectUserSection;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.equella.utils.SelectedUser;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ReloadFunction;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.model.Option;

@SuppressWarnings("nls")
@Bind
public class BulkWorkflowReassignTaskOperation extends AbstractSelectUserSection<AbstractSelectUserSection.Model>
	implements
		BulkOperationExtension
{
	private static final String BULK_VALUE = "reassignmoderator";

	@BindFactory
	public interface ReassignModeratorExecutorFactory
	{
		ReassignModeratorExecutor reassign(@Assisted("toOwner") String toOwner);
	}

	@Inject
	private TLEAclManager aclService;
	@PlugKey("bulkop.reassignmoderator")
	private static String LABEL_EXECUTE;
	@PlugKey("bulkop.reassignmoderator.title")
	private static Label LABEL_EXECUTE_TITLE;
	@PlugKey("bulkop.reassignmoderator.subtitle")
	private static Label LABEL_EXECUTE_SUBTITLE;
	@PlugKey("opresults.reassignmoderator.status")
	private static String KEY_STATUS;

	public static class ReassignModeratorExecutor implements BulkOperationExecutor
	{
		private static final long serialVersionUID = 1L;

		private final String toOwner;
		@Inject
		private ItemOperationFactory workflowFactory;
		@Inject
		private BulkWorkflowTaskOperationFactory bulkWorkflowOpFactory;

		@Inject
		public ReassignModeratorExecutor(@Assisted("toOwner") String toOwner)
		{
			this.toOwner = toOwner;
		}

		@Override
		public WorkflowOperation[] getOperations()
		{
			return new WorkflowOperation[]{bulkWorkflowOpFactory.changeModeratorAssign(toOwner),
					workflowFactory.save()};
		}

		@Override
		public String getTitleKey()
		{
			return "com.tle.web.bulk.workflowtask.bulkop.reassignmoderator.title";
		}
	}

	@Override
	public Label getStatusTitleLabel(SectionInfo info, String operationId)
	{
		return new KeyLabel(KEY_STATUS, new KeyLabel(LABEL_EXECUTE + operationId + ".title"));
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		getUserList().addEventStatements(JSHandler.EVENT_CHANGE,
			new OverrideHandler(getResultUpdater(tree, events.getEventHandler("addUsers"))));
	}

	@Override
	public BeanLocator<ReassignModeratorExecutor> getExecutor(SectionInfo info, String operationId)
	{
		List<SelectedUser> selections = getSelections(info);
		if( selections.size() == 1 )
		{
			return new FactoryMethodLocator<ReassignModeratorExecutor>(ReassignModeratorExecutorFactory.class,
				"reassign", selections.get(0).getUuid());
		}
		throw new Error("No user selected"); // shouldn't get here
	}

	@Override
	public void addOptions(SectionInfo info, List<Option<OperationInfo>> options)
	{
		if (!aclService.filterNonGrantedPrivileges(Collections.singleton("MANAGE_WORKFLOW"), true).isEmpty())
		{
			options.add(new KeyOption<OperationInfo>(LABEL_EXECUTE, BULK_VALUE, new OperationInfo(this, BULK_VALUE)));
		}
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	public boolean hasExtraOptions(SectionInfo info, String operationId)
	{
		return true;
	}

	@Override
	public boolean validateOptions(SectionInfo info, String operationId)
	{
		return !Check.isEmpty(getSelections(info));
	}

	@Override
	public boolean areOptionsFinished(SectionInfo info, String operationId)
	{
		return validateOptions(info, operationId);
	}

	@Override
	public SectionRenderable renderOptions(RenderContext context, String operationId)
	{
		this.setTitle(LABEL_EXECUTE_TITLE);
		this.setSubTitle(LABEL_EXECUTE_SUBTITLE);
		return renderSection(context, this);
	}

	@Override
	protected JSCallable getResultUpdater(SectionTree tree, ParameterizedEvent eventHandler)
	{
		if( eventHandler == null )
		{
			return new ReloadFunction(true);
		}
		return new SubmitValuesFunction(eventHandler);
	}

	@Override
	public void prepareDefaultOptions(SectionInfo info, String operationId)
	{
		// nothing
	}

	@Override
	protected SelectedUser createSelectedUser(SectionInfo info, String uuid, String displayName)
	{
		return new SelectedUser(uuid, displayName);
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
