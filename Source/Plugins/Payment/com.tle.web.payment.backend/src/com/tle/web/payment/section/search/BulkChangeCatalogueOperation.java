package com.tle.web.payment.section.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.item.ItemPack;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.guice.BindFactory;
import com.tle.core.payment.operation.ChangeCatalogueState;
import com.tle.core.payment.operation.ChangeCatalogueState.ChangeCatalogueAssignment;
import com.tle.core.payment.operation.OperationFactory;
import com.tle.core.payment.service.CatalogueService;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.core.workflow.operations.WorkflowFactory;
import com.tle.core.workflow.operations.WorkflowOperation;
import com.tle.web.bulk.operation.BulkOperationExecutor;
import com.tle.web.bulk.operation.BulkOperationExtension;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleCache;
import com.tle.web.i18n.BundleNameValue;
import com.tle.web.payment.section.search.BulkChangeCatalogueOperation.CatalogueListSectionModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.ReloadHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;

@NonNullByDefault
@Bind
public class BulkChangeCatalogueOperation extends AbstractPrototypeSection<CatalogueListSectionModel>
	implements
		BulkOperationExtension,
		HtmlRenderer
{
	private static final String BULK_VALUE_ADD = "addcatalogues"; //$NON-NLS-1$
	private static final String BULK_VALUE_REMOVE = "removecatalogues"; //$NON-NLS-1$

	@PlugKey("operation.")
	private static String KEY_NAME;

	@Inject
	private CatalogueService catalogueService;

	@Inject
	protected BundleCache bundleCache;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component(name = "cl", stateful = false)
	protected MultiSelectionList<BaseEntityLabel> catalogueList;

	private boolean forBulk;

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		catalogueList.setListModel(new CatalogueListModel());
		catalogueList.addEventStatements(JSHandler.EVENT_CHANGE, new ReloadHandler());

	}

	public List<BaseEntityLabel> getCatalogues(SectionInfo info)
	{
		return catalogueList.getSelectedValues(info);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{

		return viewFactory.createResult("cataloguelist.ftl", context); //$NON-NLS-1$
	}

	public MultiSelectionList<BaseEntityLabel> getCatalogueList()
	{
		return catalogueList;
	}

	public class CatalogueListModel extends DynamicHtmlListModel<BaseEntityLabel>
	{

		public CatalogueListModel()
		{
			setSort(true);
		}

		@Override
		protected Iterable<BaseEntityLabel> populateModel(SectionInfo info)
		{
			return catalogueService.listManageable();
		}

		@Override
		protected Option<BaseEntityLabel> convertToOption(SectionInfo info, BaseEntityLabel bent)
		{
			return new NameValueOption<BaseEntityLabel>(new BundleNameValue(bent.getBundleId(), bent.getUuid(),
				bundleCache), bent);
		}

	}

	@Override
	public Class<CatalogueListSectionModel> getModelClass()
	{
		return CatalogueListSectionModel.class;
	}

	public boolean isForBulk()
	{
		return forBulk;
	}

	public void setForBulk(boolean forBulk)
	{
		this.forBulk = forBulk;
	}

	public static class CatalogueListSectionModel
	{
		// Nothing to declare
	}

	@BindFactory
	public interface ChangeCatalogueExecutorFactory
	{
		ChangeCatalogueExecutor create(@Assisted("add") boolean add,
			@Assisted("catalogueIds") List<BaseEntityLabel> catalogueIds, @Assisted("blacklist") boolean blacklist);
	}

	public static class ChangeCatalogueExecutor implements BulkOperationExecutor
	{
		private static final long serialVersionUID = 4762593570385750424L;
		private final boolean add;
		private final List<BaseEntityLabel> catalogueIds;
		private final boolean blacklist;

		@Inject
		private OperationFactory operationFactory;
		@Inject
		private WorkflowFactory workflowFactory;

		@Inject
		public ChangeCatalogueExecutor(@Assisted("add") boolean add,
			@Assisted("catalogueIds") List<BaseEntityLabel> catalogueIds, @Assisted("blacklist") boolean blacklist)
		{
			this.add = add;
			this.catalogueIds = catalogueIds;
			this.blacklist = blacklist;
		}

		@Override
		public WorkflowOperation[] getOperations()
		{
			List<WorkflowOperation> opsList = new ArrayList<WorkflowOperation>();

			List<ChangeCatalogueAssignment> assignments = Lists.newArrayList();
			for( BaseEntityLabel catalogue : catalogueIds )
			{
				assignments.add(new ChangeCatalogueAssignment(add, catalogue.getId(), blacklist));
			}
			if( !assignments.isEmpty() )
			{
				opsList.add(operationFactory.createChangeCatalogue(new ChangeCatalogueState(assignments)));
			}

			opsList.add(workflowFactory.reIndexIfRequired());

			WorkflowOperation ops[] = opsList.toArray(new WorkflowOperation[opsList.size()]);
			return ops; // NOSONAR (kept local variable for readability)
		}

		@Override
		public String getTitleKey()
		{
			return "com.tle.web.payment.backend.bulk.changecatalogue.title";
		}
	}

	@Override
	public void addOptions(SectionInfo info, List<Option<OperationInfo>> options)
	{
		options.add(new KeyOption<OperationInfo>(KEY_NAME + BULK_VALUE_ADD, BULK_VALUE_ADD, new OperationInfo(this,
			BULK_VALUE_ADD)));
		options.add(new KeyOption<OperationInfo>(KEY_NAME + BULK_VALUE_REMOVE, BULK_VALUE_REMOVE, new OperationInfo(
			this, BULK_VALUE_REMOVE)));

	}

	@SuppressWarnings("nls")
	@Override
	public BeanLocator<? extends BulkOperationExecutor> getExecutor(SectionInfo info, String operationId)
	{
		return new FactoryMethodLocator<BulkOperationExecutor>(ChangeCatalogueExecutorFactory.class, "create", true,
			Lists.newArrayList(catalogueList.getSelectedValues(info)), operationId.equals(BULK_VALUE_REMOVE));
	}

	@Override
	public void prepareDefaultOptions(SectionInfo info, String operationId)
	{

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
		return !Check.isEmpty(catalogueList.getSelectedValues(info));
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
