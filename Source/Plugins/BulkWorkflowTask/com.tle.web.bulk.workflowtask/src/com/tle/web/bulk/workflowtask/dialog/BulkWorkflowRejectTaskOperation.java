package com.tle.web.bulk.workflowtask.dialog;

import java.util.List;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
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
@Singleton
public class BulkWorkflowRejectTaskOperation extends AbstractBulkApproveRejectSection
{
	private static final String BULK_REJECT_VAL = "rejecttasks";

	@PlugKey("bulkop.rejecttask")
	private static String STRING_REJECT;
	@PlugKey("bulkop.rejecttask.title")
	private static Label LABEL_REJECT_TITLE;
	@PlugKey("bulkop.rejecttask.subtitle")
	private static Label LABEL_REJECT_SUBTITLE;

	@BindFactory
	public interface RejectOperationExecutorFactory
	{
		RejectExecutor create(@Assisted("message") String message,
			@Assisted("stagingFolderUuid") String stagingFolderUuid,
			@Assisted("rejectAllUsers") boolean rejectAllUsers);
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	public static class RejectExecutor implements BulkOperationExecutor
	{
		private static final long serialVersionUID = 1L;
		private final String message;
		private final String stagingFolderUuid;
		private final boolean rejectAllUsers;

		@Inject
		private ItemOperationFactory workflowFactory;
		@Inject
		private BulkWorkflowTaskOperationFactory rejectOperationFactory;

		@Inject
		public RejectExecutor(@Assisted("message") String message,
			@Assisted("stagingFolderUuid") String stagingFolderUuid, @Assisted("rejectAllUsers") boolean rejectAllUsers)
		{
			this.message = message;
			this.stagingFolderUuid = stagingFolderUuid;
			this.rejectAllUsers = rejectAllUsers;
		}

		@Override
		public WorkflowOperation[] getOperations()
		{
			return new WorkflowOperation[]{rejectOperationFactory.reject(message, stagingFolderUuid, rejectAllUsers),
					workflowFactory.save()};
		}

		@Override
		public String getTitleKey()
		{
			return "com.tle.web.bulk.workflowtask.bulk.rejecttask.title";
		}
	}

	@Override
	public void addOptions(SectionInfo info, List<Option<OperationInfo>> options)
	{
		if( isOnMyTaskPage(info) )
		{
			if( hasPrivilege("REJECT_BULK_TASKS") )
			{
				options.add(new KeyOption<OperationInfo>(STRING_REJECT, BULK_REJECT_VAL,
					new OperationInfo(this, BULK_REJECT_VAL)));
			}
		}
		else
		{
			if( hasPrivilege("MANAGE_WORKFLOW") )
			{
				options.add(new KeyOption<OperationInfo>(STRING_REJECT, BULK_REJECT_VAL,
					new OperationInfo(this, BULK_REJECT_VAL)));
			}
		}
	}

	@Override
	public BeanLocator<RejectExecutor> getExecutor(SectionInfo info, String operationId)
	{
		return new FactoryMethodLocator<RejectExecutor>(RejectOperationExecutorFactory.class, "create",
			getComment(info), getModel(info).getStagingFolderUuid(), !isOnMyTaskPage(info));
	}

	@Override
	public SectionRenderable renderOptions(RenderContext context, String operationId)
	{
		setTitle(context, LABEL_REJECT_TITLE.getText());
		setSubTitle(context, LABEL_REJECT_SUBTITLE.getText());
		getModel(context).setMandatoryMessage(true);
		return renderSection(context, this);
	}

	@Override
	public Label getStatusTitleLabel(SectionInfo info, String operationId)
	{
		return LABEL_REJECT_TITLE;
	}
}
