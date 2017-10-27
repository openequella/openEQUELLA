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

import java.util.List;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.tle.annotation.Nullable;
import com.tle.core.guice.Bind;
import com.tle.core.guice.BindFactory;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.web.bulk.operation.BulkOperationExecutor;
import com.tle.web.bulk.workflowtask.BulkWorkflowTaskOperationFactory;
import com.tle.web.bulk.workflowtask.section.AbstractBulkApproveRejectSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.Option;

@Bind
public class BulkWorkflowApproveTaskOperation extends AbstractBulkApproveRejectSection
{
	private static final String BULK_APPROVE_VAL = "approvetasks";

	@PlugKey("bulkop.approvetask")
	private static String STRING_APPROVE;
	@PlugKey("bulkop.approvetask.title")
	private static Label LABEL_APPROVE_TITLE;
	@PlugKey("bulkop.approvetask.subtitle")
	private static Label LABEL_APPROVE_SUBTITLE;

	@BindFactory
	public interface ApproveOperationExecutorFactory
	{
		ApproveExecutor create(@Assisted("message") String message,
			@Assisted("acceptAllUsers") boolean acceptAllUsers);
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	public static class ApproveExecutor implements BulkOperationExecutor
	{
		private static final long serialVersionUID = 1L;
		private final String message;

		private final boolean acceptAllUsers;

		@Inject
		private ItemOperationFactory workflowFactory;
		@Inject
		private BulkWorkflowTaskOperationFactory approveOperationFactory;

		@Inject
		public ApproveExecutor(@Assisted("message") String message,
							   @Assisted("acceptAllUsers") boolean acceptAllUsers)
		{
			this.message = message;
			this.acceptAllUsers = acceptAllUsers;
		}

		@Override
		public WorkflowOperation[] getOperations()
		{
			return new WorkflowOperation[]{approveOperationFactory.approve(message, acceptAllUsers),
					workflowFactory.save()};
		}

		@Override
		public String getTitleKey()
		{
			return "com.tle.web.bulk.workflowtask.bulk.approvetask.title";
		}
	}

	@Override
	public void addOptions(SectionInfo info, List<Option<OperationInfo>> options)
	{
		if( isOnMyTaskPage(info) )
		{
			if( hasPrivilege("APPROVE_BULK_TASKS") )
			{
				options.add(new KeyOption<OperationInfo>(STRING_APPROVE, BULK_APPROVE_VAL,
					new OperationInfo(this, BULK_APPROVE_VAL)));
			}
		}
		else
		{
			if( hasPrivilege("MANAGE_WORKFLOW") )
			{
				options.add(new KeyOption<OperationInfo>(STRING_APPROVE, BULK_APPROVE_VAL,
					new OperationInfo(this, BULK_APPROVE_VAL)));
			}
		}
	}

	@Override
	public BeanLocator<ApproveExecutor> getExecutor(SectionInfo info, String operationId)
	{
		return new FactoryMethodLocator<ApproveExecutor>(ApproveOperationExecutorFactory.class, "create",
			getComment(info), !isOnMyTaskPage(info));
	}

	@Override
	public SectionRenderable renderOptions(RenderContext context, String operationId)
	{
		setTitle(context, LABEL_APPROVE_TITLE.getText());
		setSubTitle(context, LABEL_APPROVE_SUBTITLE.getText());
		return renderSection(context, this);
	}

	@Override
	public Label getStatusTitleLabel(SectionInfo info, String operationId)
	{
		return LABEL_APPROVE_TITLE;
	}
}
