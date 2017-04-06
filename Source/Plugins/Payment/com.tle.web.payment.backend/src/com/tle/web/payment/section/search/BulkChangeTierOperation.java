package com.tle.web.payment.section.search;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Strings;
import com.google.inject.assistedinject.Assisted;
import com.tle.beans.item.ItemPack;
import com.tle.core.guice.Bind;
import com.tle.core.guice.BindFactory;
import com.tle.core.payment.operation.ChangeTierOperation;
import com.tle.core.payment.operation.ChangeTierState;
import com.tle.core.payment.operation.OperationFactory;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.core.workflow.operations.WorkflowFactory;
import com.tle.core.workflow.operations.WorkflowOperation;
import com.tle.web.bulk.operation.BulkOperationExecutor;
import com.tle.web.bulk.operation.BulkOperationExtension;
import com.tle.web.itemadmin.section.ItemAdminResultsDialog;
import com.tle.web.payment.viewitem.section.ChangePricingTierSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.model.Option;

@Bind
public class BulkChangeTierOperation extends ChangePricingTierSection implements BulkOperationExtension
{
	private static final String BULK_VALUE = "changetier"; //$NON-NLS-1$

	@PlugKey("operation.")
	private static String KEY_NAME;

	@TreeLookup
	private ItemAdminResultsDialog bulkDialog;

	@BindFactory
	public interface ChangeTierExecutorFactory
	{
		ChangeTierExecutor create(boolean free, @Assisted("purchaseUuid") String purchaseUuid,
			@Assisted("subscriptionUuid") String subscriptionUuid);
	}

	public static class ChangeTierExecutor implements BulkOperationExecutor
	{
		private static final long serialVersionUID = 2944235592558019347L;

		private final boolean free;
		private final String purchaseUuid;
		private final String subscriptionUuid;

		@Inject
		private OperationFactory operationFactory;
		@Inject
		private WorkflowFactory workflowFactory;

		@Inject
		public ChangeTierExecutor(@Assisted boolean free, @Assisted("purchaseUuid") String purchaseUuid,
			@Assisted("subscriptionUuid") String subscriptionUuid)
		{
			this.free = free;
			this.purchaseUuid = purchaseUuid;
			this.subscriptionUuid = subscriptionUuid;
		}

		@Override
		public WorkflowOperation[] getOperations()
		{
			WorkflowOperation[] ops;
			ChangeTierOperation changeTier = operationFactory.createChangeTier(new ChangeTierState(free, purchaseUuid,
				subscriptionUuid));
			ops = new WorkflowOperation[]{changeTier, workflowFactory.reindexOnly(false)};
			return ops;
		}

		@Override
		public String getTitleKey()
		{
			return "com.tle.web.payment.backend.bulk.changetier.title";
		}
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		setForBulk(true);
		super.registered(id, tree);
	}

	@Override
	public void addOptions(SectionInfo info, List<Option<OperationInfo>> options)
	{
		options
			.add(new KeyOption<OperationInfo>(KEY_NAME + BULK_VALUE, BULK_VALUE, new OperationInfo(this, BULK_VALUE)));

	}

	@SuppressWarnings("nls")
	@Override
	public BeanLocator<? extends BulkOperationExecutor> getExecutor(SectionInfo info, String operationId)
	{
		return new FactoryMethodLocator<BulkOperationExecutor>(ChangeTierExecutorFactory.class, "create",
			freeBox.isChecked(info), Strings.nullToEmpty(purchaseTierList.getSelectedValueAsString(info)),
			Strings.nullToEmpty(selectedSubscriptionTier.getFirstChecked(info)));
	}

	@Override
	public void prepareDefaultOptions(SectionInfo info, String operationId)
	{
		// none
	}

	@Override
	public SectionRenderable renderOptions(RenderContext context, String operationId)
	{
		return renderSection(context, this);
	}

	@SuppressWarnings("nls")
	@Override
	public Label getStatusTitleLabel(SectionInfo info, String operationId)
	{
		return new KeyLabel(KEY_NAME + operationId + ".status");
	}

	@Override
	public boolean areOptionsFinished(SectionInfo info, String operationId)
	{
		return bulkDialog.getModel(info).isShowOptions();
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
