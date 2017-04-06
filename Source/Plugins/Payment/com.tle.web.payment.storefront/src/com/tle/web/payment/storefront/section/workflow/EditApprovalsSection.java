package com.tle.web.payment.storefront.section.workflow;

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.common.Check;
import com.tle.common.recipientselector.ExpressionFormatter;
import com.tle.core.services.user.UserService;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.recipientselector.ExpressionSelectorDialog;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@NonNullByDefault
@TreeIndexed
public class EditApprovalsSection extends AbstractPrototypeSection<EditApprovalsSection.EditApprovalsSectionModel>
	implements
		HtmlRenderer,
		ModalApprovalSection
{
	@PlugKey("approvals.label.pagetitle.new")
	private static Label LABEL_CREATE_PAGETITLE;
	@PlugKey("approvals.label.pagetitle.edit")
	private static Label LABEL_EDIT_PAGETITLE;
	@PlugKey("payments.label.pagetitle.new")
	private static Label LABEL_CREATE_PAGETITLE_PAYMENTS;
	@PlugKey("payments.label.pagetitle.edit")
	private static Label LABEL_EDIT_PAGETITLE_PAYMENTS;
	@PlugKey("payments.edit.select")
	private static Label EXPRESSION_SELECTOR_TITLE_LABEL;

	@PlugKey("approvals.edit.from")
	private static Label LABEL_APPROVALS_FROM;
	@PlugKey("approvals.edit.to")
	private static Label LABEL_APPROVALS_TO;
	@PlugKey("payments.edit.from")
	private static Label LABEL_PAYMENTS_FROM;
	@PlugKey("payments.edit.to")
	private static Label LABEL_PAYMENTS_TO;

	@PlugKey("approvals.edit.from.help")
	private static Label LABEL_APPROVALS_FROM_HELP;
	@PlugKey("approvals.edit.to.help")
	private static Label LABEL_APPROVALS_TO_HELP;
	@PlugKey("payments.edit.from.help")
	private static Label LABEL_PAYMENTS_FROM_HELP;
	@PlugKey("payments.edit.to.help")
	private static Label LABEL_PAYMENTS_TO_HELP;

	@PlugKey("approvals.edit.mandatory")
	private static Label LABEL_ERROR_MANDATORY;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;

	@Inject
	private ComponentFactory componentFactory;
	@Inject
	private UserSessionService sessionService;
	@Inject
	private UserService userService;

	@TreeLookup
	private RootApprovalsSection rootSection;
	@TreeLookup
	private ShowApprovalsSection showSection;

	@PlugKey("approvals.button.save")
	@Component(name = "sv", stateful = false)
	private Button saveButton;
	@PlugKey("approvals.button.cancel")
	@Component(name = "cl", stateful = false)
	private Button cancelButton;

	@Inject
	private ExpressionSelectorDialog userSelector;
	@Inject
	private ExpressionSelectorDialog approverSelector;

	@SuppressWarnings("nls")
	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		saveButton.setClickHandler(events.getNamedHandler("save"));
		cancelButton.setClickHandler(events.getNamedHandler("cancel"));

		// TODO: don't know why the label isn't defined
		userSelector.setTitle(EXPRESSION_SELECTOR_TITLE_LABEL);
		userSelector.setTitle(null); // set null to use the default title
		userSelector.setOkCallback(events.getSubmitValuesFunction("userExpression"));
		componentFactory.registerComponent(id, "userSelector", tree, userSelector);

		approverSelector.setOnlyUsersAndGroups(true);
		approverSelector.setTitle(EXPRESSION_SELECTOR_TITLE_LABEL);
		approverSelector.setTitle(null);// set null to use the default title

		approverSelector.setOkCallback(events.getSubmitValuesFunction("approverExpression"));
		componentFactory.registerComponent(id, "approverSelector", tree, approverSelector);
	}

	@Override
	@SuppressWarnings("nls")
	public SectionResult renderHtml(RenderEventContext context)
	{
		SelectedApproval approval = loadSession(context);

		EditApprovalsSectionModel model = getModel(context);

		String userExpression = approval.getExpressionFrom();
		if( Check.isEmpty(userExpression) )
		{
			model.setUserPretty("");
		}
		else
		{
			model.setUserPretty(new ExpressionFormatter(userService).convertToInfix(userExpression));
		}

		String approverExpression = approval.getExpressionTo();
		if( Check.isEmpty(approverExpression) )
		{
			model.setApproverPretty("");
		}
		else
		{
			model.setApproverPretty(new ExpressionFormatter(userService).convertToInfix(approverExpression));
		}

		model.setFromLabel(approval.isApproval() ? LABEL_APPROVALS_FROM : LABEL_PAYMENTS_FROM);
		model.setToLabel(approval.isApproval() ? LABEL_APPROVALS_TO : LABEL_PAYMENTS_TO);
		model.setFromHelp(approval.isApproval() ? LABEL_APPROVALS_FROM_HELP : LABEL_PAYMENTS_FROM_HELP);
		model.setToHelp(approval.isApproval() ? LABEL_APPROVALS_TO_HELP : LABEL_PAYMENTS_TO_HELP);
		model.setErrors(approval.getValidationErrors());

		userSelector.setExpression(context, approval.getExpressionFrom());
		approverSelector.setExpression(context, approval.getExpressionTo());

		final GenericTemplateResult templateResult = new GenericTemplateResult();
		templateResult.addNamedResult("body", view.createResult("editapproval.ftl", context));
		return templateResult;
	}

	@EventHandlerMethod
	public void userExpression(SectionInfo info, String selectorId, String expression) throws Exception
	{
		SelectedApproval approval = loadSession(info);
		approval.setExpressionFrom(expression);
		if( !Check.isEmpty(expression) )
		{
			approval.getValidationErrors().remove("from"); //$NON-NLS-1$
		}
	}

	@EventHandlerMethod
	public void approverExpression(SectionInfo info, String selectorId, String expression) throws Exception
	{
		SelectedApproval approval = loadSession(info);
		approval.setExpressionTo(expression);
		if( !Check.isEmpty(expression) )
		{
			approval.getValidationErrors().remove("to"); //$NON-NLS-1$
		}
	}

	@EventHandlerMethod
	public void cancel(SectionInfo info)
	{
		cancelSession(info);
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		SelectedApproval approval = loadSession(info);

		validate(info, approval);

		if( approval.getValidationErrors().isEmpty() )
		{
			cancelSession(info);
			showSection.addApproval(info, approval);
		}
	}

	@Override
	public Class<EditApprovalsSectionModel> getModelClass()
	{
		return EditApprovalsSectionModel.class;
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_MODAL_LOGIC)
	public void checkModal(SectionInfo info)
	{
		EditApprovalsSectionModel model = getModel(info);
		if( !Check.isEmpty(model.getSessionId()) )
		{
			rootSection.setModalSection(info, this);
		}
	}

	public void editApproval(SectionInfo info, SelectedApproval approval)
	{
		final EditApprovalsSectionModel model = getModel(info);
		String string = UUID.randomUUID().toString();
		model.setSessionId(string);
		sessionService.setAttribute(model.getSessionId(), approval);
	}

	@DirectEvent
	public void loadFromSession(SectionInfo info)
	{
		EditApprovalsSectionModel model = getModel(info);
		model.setRendered(true);

		if( !Check.isEmpty(model.getSessionId()) )
		{
			SelectedApproval approval = loadSession(info);
			userSelector.setExpression(info, approval.getExpressionFrom());
			approverSelector.setExpression(info, approval.getExpressionTo());
		}
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_BEFORE_EVENTS)
	public void includeHandler(SectionInfo info)
	{
		EditApprovalsSectionModel model = getModel(info);
		if( model.isRendered() )
		{
			final String sessionId = model.getSessionId();
			if( sessionId != null )
			{
				SelectedApproval approval = loadSession(info);
				approval.setExpressionFrom(userSelector.getExpression(info));
				approval.setExpressionTo(approverSelector.getExpression(info));
				sessionService.setAttribute(model.getSessionId(), approval);
			}

		}
	}

	@SuppressWarnings("nls")
	private void validate(SectionInfo info, SelectedApproval approval)
	{
		Map<String, Object> errors = approval.getValidationErrors();
		errors.clear();

		String expression = userSelector.getExpression(info);
		if( Check.isEmpty(expression) || expression.trim().isEmpty() )
		{
			errors.put("from", LABEL_ERROR_MANDATORY);
		}

		expression = approverSelector.getExpression(info);
		if( Check.isEmpty(expression) || expression.trim().isEmpty() )
		{
			errors.put("to", LABEL_ERROR_MANDATORY);
		}

	}

	private SelectedApproval loadSession(SectionInfo info)
	{
		SelectedApproval approval = sessionService.getAttribute(getModel(info).getSessionId());
		return approval; // NOSONAR (kept local variable for readability)
	}

	private void cancelSession(SectionInfo info)
	{
		EditApprovalsSectionModel model = getModel(info);
		sessionService.removeAttribute(model.getSessionId());
		model.setSessionId(null);
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	@NonNullByDefault(false)
	public static class EditApprovalsSectionModel
	{
		@Bookmarked(name = "s")
		private String sessionId;
		@Bookmarked(stateful = false)
		private boolean rendered;
		private Label pageTitle;
		private String userPretty;
		private String approverPretty;
		private Label fromLabel;
		private Label toLabel;
		private Label fromHelp;
		private Label toHelp;
		private Map<String, Object> errors = Maps.newHashMap();

		public Label getPageTitle()
		{
			return pageTitle;
		}

		public void setPageTitle(Label pageTitle)
		{
			this.pageTitle = pageTitle;
		}

		public String getSessionId()
		{
			return sessionId;
		}

		public void setSessionId(String sessionId)
		{
			this.sessionId = sessionId;
		}

		public boolean isRendered()
		{
			return rendered;
		}

		public void setRendered(boolean rendered)
		{
			this.rendered = rendered;
		}

		public String getUserPretty()
		{
			return userPretty;
		}

		public void setUserPretty(String expressionPretty)
		{
			this.userPretty = expressionPretty;
		}

		public String getApproverPretty()
		{
			return approverPretty;
		}

		public void setApproverPretty(String approverPretty)
		{
			this.approverPretty = approverPretty;
		}

		public Label getFromLabel()
		{
			return fromLabel;
		}

		public void setFromLabel(Label fromLabel)
		{
			this.fromLabel = fromLabel;
		}

		public Label getToLabel()
		{
			return toLabel;
		}

		public void setToLabel(Label toLabel)
		{
			this.toLabel = toLabel;
		}

		public Label getFromHelp()
		{
			return fromHelp;
		}

		public void setFromHelp(Label fromHelp)
		{
			this.fromHelp = fromHelp;
		}

		public Label getToHelp()
		{
			return toHelp;
		}

		public void setToHelp(Label toHelp)
		{
			this.toHelp = toHelp;
		}

		public Map<String, Object> getErrors()
		{
			return errors;
		}

		public void setErrors(Map<String, Object> errors)
		{
			this.errors = errors;
		}

	}

	private Label getPageTitle(SectionInfo info)
	{
		SelectedApproval approval = loadSession(info);
		if( approval.getApprovalIndex() == -1 )
		{
			return approval.isApproval() ? LABEL_CREATE_PAGETITLE : LABEL_CREATE_PAGETITLE_PAYMENTS;
		}

		return approval.isApproval() ? LABEL_EDIT_PAGETITLE : LABEL_EDIT_PAGETITLE_PAYMENTS;
	}

	@Override
	public void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		ContentLayout.setLayout(info, ContentLayout.ONE_COLUMN);

		EditApprovalsSectionModel model = getModel(info);
		final Label pageTitle = getPageTitle(info);

		decorations.setContentBodyClass("approvaledit"); //$NON-NLS-1$
		crumbs.setForcedLastCrumb(pageTitle);

		model.setPageTitle(pageTitle);
		decorations.setTitle(pageTitle);
	}

	public ExpressionSelectorDialog getUserSelector()
	{
		return userSelector;
	}

	public ExpressionSelectorDialog getApproverSelector()
	{
		return approverSelector;
	}
}
