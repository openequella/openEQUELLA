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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.tle.beans.item.ItemPack;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.guice.BindFactory;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.web.bulk.operation.BulkOperationExecutor;
import com.tle.web.bulk.operation.BulkOperationExtension;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
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

@Bind
public class BulkAddCollaboratorsDialog extends AbstractSelectUserSection<AbstractSelectUserSection.Model>
	implements
		BulkOperationExtension
{

	private static final String BULK_VALUE = "addcollaborators";

	@BindFactory
	public interface AddCollaboratorExecutorFactory
	{
		AddCollaboratorExecutor create(@Assisted("collabs") Set<String> collaborators);
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		setMultipleUsers(true);
		tree.registerInnerSection(this, parentId);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		getUserList().addEventStatements(JSHandler.EVENT_CHANGE,
			new OverrideHandler(getResultUpdater(tree, events.getEventHandler("addUsers"))));
	}

	@Override
	public void addOptions(SectionInfo info, List<Option<OperationInfo>> options)
	{
		options.add(new KeyOption<OperationInfo>(StandardOperations.getNameKey() + BULK_VALUE, BULK_VALUE,
			new OperationInfo(this, BULK_VALUE)));
	}

	@Override
	public BeanLocator<? extends BulkOperationExecutor> getExecutor(SectionInfo info, String operationId)
	{
		List<SelectedUser> users = getSelections(info);
		if( users.size() > 0 )
		{
			// hashet is serializable, hence the cast
			HashSet<String> selections = (HashSet<String>) users.stream().map(u -> u.getUuid())
				.collect(Collectors.toSet());
			return new FactoryMethodLocator<AddCollaboratorExecutor>(AddCollaboratorExecutorFactory.class, "create",
				selections);
		}
		throw new Error("No user selected"); // shouldn't get here
	}

	public static class AddCollaboratorExecutor implements BulkOperationExecutor
	{
		private final Set<String> collaborators;
		@Inject
		private ItemOperationFactory workflowFactory;

		@Inject
		public AddCollaboratorExecutor(@Assisted("collabs") Set<String> collaborators)
		{
			this.collaborators = collaborators;
		}

		@Override
		public WorkflowOperation[] getOperations()
		{
			return new WorkflowOperation[]{workflowFactory.addCollaborators(collaborators), workflowFactory.save()};
		}

		@Override
		public String getTitleKey()
		{
			return "com.tle.web.itemadmin.bulk." + BULK_VALUE + ".title";
		}
	}

	@Override
	public void prepareDefaultOptions(SectionInfo info, String operationId)
	{
		// nada
	}

	@Override
	public SectionRenderable renderOptions(RenderContext context, String operationId)
	{
		return renderSection(context, this);
	}

	@Override
	public Label getStatusTitleLabel(SectionInfo info, String operationId)
	{
		return new KeyLabel(StandardOperations.getStatusKey(),
			new KeyLabel(StandardOperations.getNameKey() + operationId + ".title"));
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
	public boolean hasExtraOptions(SectionInfo info, String operationId)
	{
		return true;
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
	public ItemPack runPreview(SectionInfo info, String operationId, long itemId) throws Exception
	{
		return null;
	}

	@Override
	public boolean showPreviousButton(SectionInfo info, String opererationId)
	{
		return true;
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
	protected SelectedUser createSelectedUser(SectionInfo info, String uuid, String displayName)
	{
		return new SelectedUser(uuid, displayName);
	}
}
