package com.tle.web.payment.storefront.section.workflow;

import java.util.List;

import javax.inject.Inject;

import net.sf.json.JSONObject;

import com.tle.common.payment.storefront.entity.ApprovalsPaymentsSettings;
import com.tle.common.payment.storefront.entity.ApprovalsPaymentsSettings.ApprovalsPayments;
import com.tle.common.recipientselector.ExpressionFormatter;
import com.tle.core.guice.Bind;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.user.UserService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.payment.storefront.section.workflow.ShowApprovalsSection.ShowApprovalsSectionModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;

@Bind
@TreeIndexed
public class ShowApprovalsSection extends AbstractPrototypeSection<ShowApprovalsSectionModel> implements HtmlRenderer
{
	@PlugKey("approvals.title")
	private static Label TITLE_LABEL;

	@PlugKey("approvals.table.purchaser")
	private static Label COLUMN_PURCHASER;
	@PlugKey("approvals.table.approver")
	private static Label COLUMN_APPROVER;

	@PlugKey("payments.table.purchaser.approvers")
	private static Label COLUMN_PURCHASER_APPROVER;
	@PlugKey("payments.table.payers")
	private static Label COLUMN_PAYERS;

	@PlugKey("approvals.label.edit")
	private static Label LABEL_EDIT_APPROVAL;
	@PlugKey("approvals.label.remove")
	private static Label LABEL_REMOVE_APPROVAL;
	@PlugKey("approvals.label.confirm.remove")
	private static Confirm LABEL_CONFIRM_REMOVE;
	@PlugKey("approvals.table.empty")
	private static Label LABEL_NO_RULES;
	@PlugKey("approvals.label.moveup")
	private static Label LABEL_MOVE_UP;
	@PlugKey("approvals.label.movedown")
	private static Label LABEL_MOVE_DOWN;
	@PlugKey("approvals.label.invalidexpression")
	private static Label LABEL_INVALID_EXPRESSION;

	@PlugURL("images/up.gif")
	private static String URL_ICON_UP;
	@PlugURL("images/down.gif")
	private static String URL_ICON_DOWN;

	@Inject
	private ConfigurationService configService;
	@Inject
	private UserService userService;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;
	@AjaxFactory
	private AjaxGenerator ajax;

	@TreeLookup
	private EditApprovalsSection editApprovalsSection;

	@Component
	private SelectionsTable approvalsTable;
	@Component
	private SelectionsTable paymentsTable;
	@PlugKey("approvals.table.link.add")
	@Component(name = "aal")
	private Link addApprovalLink;
	@PlugKey("payments.table.link.add")
	@Component(name = "apl")
	private Link addPaymentLink;
	@PlugKey("approvals.label.enabled")
	@Component(name = "e", stateful = false)
	private Checkbox enabled;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final ShowApprovalsSectionModel model = getModel(context);
		model.setPageTitle(TITLE_LABEL);

		enabled.setChecked(context, getConfig().isEnabled());

		final GenericTemplateResult templateResult = new GenericTemplateResult();
		templateResult.addNamedResult(OneColumnLayout.BODY, view.createResult("approvals.ftl", this));
		return templateResult;
	}

	@SuppressWarnings("nls")
	@Override
	public void registered(String id, final SectionTree tree)
	{
		super.registered(id, tree);

		SubmitValuesFunction editFunc = events.getSubmitValuesFunction("editApproval");
		UpdateDomFunction remvove = getAjaxUpdate(tree, "removeApproval", "approvalsTable", "paymentsTable");
		UpdateDomFunction shift = getAjaxUpdate(tree, "shiftApproval", "approvalsTable", "paymentsTable");

		approvalsTable.setColumnHeadings(COLUMN_PURCHASER, COLUMN_APPROVER, "");
		approvalsTable.setNothingSelectedText(LABEL_NO_RULES);
		approvalsTable.setAddAction(addApprovalLink);
		approvalsTable.setSelectionsModel(new ApprovalsTableModel(true, editFunc, remvove, shift));

		addApprovalLink.setStyleClass("add");
		addApprovalLink.setClickHandler(new OverrideHandler(editFunc, true, -1, "", ""));

		paymentsTable.setColumnHeadings(COLUMN_PURCHASER_APPROVER, COLUMN_PAYERS, "");
		paymentsTable.setNothingSelectedText(LABEL_NO_RULES);
		paymentsTable.setAddAction(addPaymentLink);
		paymentsTable.setSelectionsModel(new ApprovalsTableModel(false, editFunc, remvove, shift));

		addPaymentLink.setStyleClass("add");
		addPaymentLink.setClickHandler(new OverrideHandler(editFunc, false, -1, "", ""));

		enabled.setClickHandler(events.getNamedHandler("toggleEnabled"));
	}

	@EventHandlerMethod
	public void editApproval(SectionInfo info, boolean approval, int index, String expressionTo, String fromExression)
	{
		SelectedApproval selectedApproval = new SelectedApproval(index, fromExression, expressionTo, approval);
		editApprovalsSection.editApproval(info, selectedApproval);
	}

	@EventHandlerMethod
	public void toggleEnabled(SectionInfo info)
	{
		final ApprovalsPaymentsSettings config = getConfig();
		config.setEnabled(enabled.isChecked(info));
		configService.setProperties(config);
	}

	@EventHandlerMethod
	public void removeApproval(SectionInfo info, boolean approval, int index)
	{
		final ApprovalsPaymentsSettings config = getConfig();
		if( approval )
		{
			config.getApprovals().remove(index);
		}
		else
		{
			config.getPayments().remove(index);
		}
		configService.setProperties(config);
	}

	@EventHandlerMethod
	public void shiftApproval(SectionInfo info, boolean approval, int index, boolean up)
	{
		final ApprovalsPaymentsSettings config = getConfig();

		final List<ApprovalsPayments> approvals = approval ? config.getApprovals() : config.getPayments();
		final ApprovalsPayments uq = approvals.get(index);

		int i = index;
		if( up && index > 0 )
		{
			i--;
		}
		else if( !up && index < approvals.size() - 1 )
		{
			i++;
		}
		approvals.remove(index);
		approvals.add(i, uq);

		configService.setProperties(config);
	}

	private UpdateDomFunction getAjaxUpdate(SectionTree tree, String eventHandlerName, String... ajaxIds)
	{
		return ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler(eventHandlerName),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), ajaxIds);
	}

	public void addApproval(SectionInfo info, SelectedApproval approval)
	{
		final ApprovalsPaymentsSettings config = getConfig();

		final ApprovalsPayments ap;
		if( approval.getApprovalIndex() == -1 )
		{
			ap = new ApprovalsPayments();
			(approval.isApproval() ? config.getApprovals() : config.getPayments()).add(ap);
		}
		else
		{
			ap = (approval.isApproval() ? config.getApprovals() : config.getPayments())
				.get(approval.getApprovalIndex());
		}
		ap.setExpressionTo(approval.getExpressionTo());
		ap.setExpressionFrom(approval.getExpressionFrom());

		configService.setProperties(config);
	}

	private ApprovalsPaymentsSettings getConfig()
	{
		return configService.getProperties(new ApprovalsPaymentsSettings());
	}

	public static SelectedApproval approvalFromJsonString(String json)
	{
		final SelectedApproval approval = (SelectedApproval) JSONObject.toBean(JSONObject.fromObject(json),
			SelectedApproval.class);
		return approval;
	}

	public SelectionsTable getApprovalsTable()
	{
		return approvalsTable;
	}

	public Link getAddApprovalLink()
	{
		return addApprovalLink;
	}

	@Override
	public Class<ShowApprovalsSectionModel> getModelClass()
	{
		return ShowApprovalsSectionModel.class;
	}

	public SelectionsTable getPaymentsTable()
	{
		return paymentsTable;
	}

	private final class ApprovalsTableModel extends DynamicSelectionsTableModel<ApprovalsPayments>
	{
		private final boolean approval;
		private final SubmitValuesFunction editFunc;
		private final UpdateDomFunction deleteFunc;
		private final UpdateDomFunction upDownFunc;

		private ApprovalsTableModel(boolean approval, SubmitValuesFunction editFunc, UpdateDomFunction deleteFunc,
			UpdateDomFunction upDownFunc)
		{
			this.approval = approval;
			this.editFunc = editFunc;
			this.deleteFunc = deleteFunc;
			this.upDownFunc = upDownFunc;
		}

		@Override
		protected List<ApprovalsPayments> getSourceList(SectionInfo info)
		{
			return approval ? getConfig().getApprovals() : getConfig().getPayments();
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, ApprovalsPayments approvalPay,
			List<SectionRenderable> actions, int index)
		{
			try
			{
				selection.setName(new TextLabel(new ExpressionFormatter(userService).convertToInfix(approvalPay
					.getExpressionFrom())));
			}
			catch( Exception e )
			{
				selection.setName(LABEL_INVALID_EXPRESSION);
			}
			Label expressionLabel;
			try
			{
				expressionLabel = new TextLabel(new ExpressionFormatter(userService).convertToInfix(approvalPay
					.getExpressionTo()));
			}
			catch( Exception e )
			{
				expressionLabel = LABEL_INVALID_EXPRESSION;
			}
			selection.addColumn(expressionLabel).addClass("name");

			actions.add(makeAction(
				LABEL_EDIT_APPROVAL,
				new OverrideHandler(editFunc, approval, index, approvalPay.getExpressionTo(), approvalPay
					.getExpressionFrom())));

			actions.add(makeAction(LABEL_REMOVE_APPROVAL,
				new OverrideHandler(deleteFunc, approval, index).addValidator(LABEL_CONFIRM_REMOVE)));

			final HtmlLinkState upLinkState = new HtmlLinkState(new OverrideHandler(upDownFunc, approval, index, true));
			final LinkRenderer upLink = new LinkRenderer(upLinkState);
			upLink.setNestedRenderable(new ImageRenderer(URL_ICON_UP, LABEL_MOVE_UP));

			final HtmlLinkState downLinkState = new HtmlLinkState(new OverrideHandler(upDownFunc, approval, index,
				false));
			final LinkRenderer downLink = new LinkRenderer(downLinkState);
			downLink.setNestedRenderable(new ImageRenderer(URL_ICON_DOWN, LABEL_MOVE_DOWN));

			// Up and down in same action column
			actions.add(CombinedRenderer.combineMultipleResults(upLink, downLink));
		}
	}

	public Checkbox getEnabled()
	{
		return enabled;
	}

	public static class ShowApprovalsSectionModel
	{
		private Label pageTitle;

		public Label getPageTitle()
		{
			return pageTitle;
		}

		public void setPageTitle(Label pageTitle)
		{
			this.pageTitle = pageTitle;
		}
	}
}
