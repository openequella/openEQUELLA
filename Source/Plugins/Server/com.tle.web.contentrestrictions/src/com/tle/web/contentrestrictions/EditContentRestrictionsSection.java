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

package com.tle.web.contentrestrictions;

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import com.dytech.common.GeneralConstants;
import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.common.Check;
import com.tle.common.recipientselector.ExpressionFormatter;
import com.tle.core.services.user.UserService;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.contentrestrictions.dialog.SelectedQuota;
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
import com.tle.web.sections.events.js.SubmitValuesHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
@TreeIndexed
public class EditContentRestrictionsSection
	extends
		AbstractPrototypeSection<EditContentRestrictionsSection.EditContentRestrictionsSectionModel>
	implements
		HtmlRenderer,
		ModalContentRestrictionsSection
{

	@PlugKey("addquota.dialog.button.ok")
	@Component(name = "sv", stateful = false)
	private Button saveButton;
	@PlugKey("addquota.dialog.button.cancel")
	@Component(name = "cl", stateful = false)
	private Button cancelButton;

	@PlugKey("contentrestrictions.title")
	private static Label TITLE_LABEL;
	@PlugKey("contentrestrictions.title.return")
	private static Label BREADCRUMB_TITLE_LABEL;

	@PlugKey("addquota.dialog.error.validation.invalidquota")
	private static Label LABEL_INVALID_QUOTA;
	@PlugKey("addquota.dialog.error.validation.impossiblequota")
	private static Label LABEL_IMPOSSIBLE_QUOTA;
	@PlugKey("addquota.dialog.label.expression")
	private static Label LABEL_EMPTY_EXPRESSION;

	@PlugKey("addquota.dialog.heading.label.add")
	private static Label LABEL_HEADING_ADD;

	@PlugKey("addquota.dialog.heading.label.edit")
	private static Label LABEL_HEADING_EDIT;

	@Component(name = "qs")
	private TextField quotaSizeField;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;

	@TreeLookup
	private RootContentRestrictionsSection rootSection;

	@Inject
	private ExpressionSelectorDialog userSelector;

	@Inject
	private ComponentFactory componentFactory;

	@Inject
	private UserSessionService sessionService;
	@Inject
	private UserService userService;

	private SubmitValuesHandler cancelFunc;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		saveButton.setClickHandler(events.getNamedHandler("save"));

		cancelFunc = events.getNamedHandler("cancel");
		cancelButton.setClickHandler(cancelFunc);

		userSelector.setTitle(null); // set null to use the default title
		userSelector.setOkCallback(events.getSubmitValuesFunction("userExpression"));
		componentFactory.registerComponent(id, "userSelector", tree, userSelector);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		SelectedQuota selected = loadSession(context);
		EditContentRestrictionsSectionModel model = getModel(context);

		if( selected.getQuotaIndex() == -1 )
		{
			model.setHeading(LABEL_HEADING_ADD);
		}
		else
		{
			model.setHeading(LABEL_HEADING_EDIT);
		}

		String userExpression = selected.getExpression();
		if( Check.isEmpty(userExpression) )
		{
			model.setExpressionPretty("");
		}
		else
		{
			model.setExpressionPretty(new ExpressionFormatter(userService).convertToInfix(userExpression));
		}

		quotaSizeField.setValue(context, Long.toString(selected.getQuota() / GeneralConstants.BYTES_PER_MEGABYTE));

		model.setErrors(selected.getValidationErrors());

		final GenericTemplateResult templateResult = new GenericTemplateResult();
		templateResult.addNamedResult("body", view.createResult("adduserandquota.ftl", this));
		return templateResult;
	}

	@EventHandlerMethod
	public void userExpression(SectionInfo info, String selectorId, String expression)
	{
		SelectedQuota selected = loadSession(info);
		selected.setExpression(expression);
		if( !Check.isEmpty(expression) )
		{
			selected.getValidationErrors().remove("expression");
		}
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		SelectedQuota selected = loadSession(info);
		if( selected.getValidationErrors().isEmpty() )
		{
			cancelSession(info);
			rootSection.addUserAndQuota(info, selected);
		}
	}

	private void validate(SectionInfo info, SelectedQuota selected)
	{
		Map<String, Object> errors = selected.getValidationErrors();
		errors.clear();

		String expression = userSelector.getExpression(info);
		if( Check.isEmpty(expression) || expression.trim().isEmpty() )
		{
			errors.put("expression", LABEL_EMPTY_EXPRESSION);
		}

		// try to parse quota size
		long qs = 0;
		try
		{
			qs = Long.parseLong(quotaSizeField.getValue(info));
		}
		catch( Exception e )
		{
			errors.put("userquota", LABEL_INVALID_QUOTA);
		}

		if( qs < 0 )
		{
			errors.put("userquota", LABEL_IMPOSSIBLE_QUOTA);
		}
		// if the multiplier is an overrun because the input is impossibly
		// large, then qs would be negative
		qs *= GeneralConstants.BYTES_PER_MEGABYTE;

		if( qs < 0 )
		{
			errors.put("userquota", LABEL_IMPOSSIBLE_QUOTA);
		}
	}

	@EventHandlerMethod
	public void cancel(SectionInfo info)
	{
		cancelSession(info);
	}

	private void cancelSession(SectionInfo info)
	{
		EditContentRestrictionsSectionModel model = getModel(info);
		sessionService.removeAttribute(model.getSessionId());
		model.setSessionId(null);
	}

	private SelectedQuota loadSession(SectionInfo info)
	{
		SelectedQuota selected = sessionService.getAttribute(getModel(info).getSessionId());
		return selected; // NOSONAR (keeping local variable for readability)
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_MODAL_LOGIC)
	public void checkModal(SectionInfo info)
	{
		EditContentRestrictionsSectionModel model = getModel(info);
		if( !Check.isEmpty(model.getSessionId()) )
		{
			rootSection.setModalSection(info, this);
		}
	}

	public void editUserQuota(SectionInfo info, SelectedQuota selected)
	{
		final EditContentRestrictionsSectionModel model = getModel(info);
		String string = UUID.randomUUID().toString();
		model.setSessionId(string);
		sessionService.setAttribute(model.getSessionId(), selected);
	}

	@DirectEvent
	public void loadFromSession(SectionInfo info)
	{
		EditContentRestrictionsSectionModel model = getModel(info);
		model.setRendered(true);

		if( !Check.isEmpty(model.getSessionId()) )
		{
			SelectedQuota selected = loadSession(info);
			userSelector.setExpression(info, selected.getExpression());
			quotaSizeField.setValue(info, Long.toString(selected.getQuota() / GeneralConstants.BYTES_PER_MEGABYTE));
		}
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_BEFORE_EVENTS)
	public void includeHandler(SectionInfo info)
	{
		EditContentRestrictionsSectionModel model = getModel(info);
		if( model.isRendered() )
		{
			final String sessionId = model.getSessionId();
			if( sessionId != null )
			{
				SelectedQuota selected = loadSession(info);
				validate(info, selected);
				if( !selected.getValidationErrors().containsKey("expression") )
				{
					selected.setExpression(userSelector.getExpression(info));
				}

				if( !selected.getValidationErrors().containsKey("userquota") )
				{
					selected.setQuota(Long.parseLong(quotaSizeField.getValue(info))
						* GeneralConstants.BYTES_PER_MEGABYTE);
				}
				sessionService.setAttribute(model.getSessionId(), selected);
			}
		}
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public TextField getQuotaSizeField()
	{
		return quotaSizeField;
	}

	public ExpressionSelectorDialog getUserSelector()
	{
		return userSelector;
	}

	@Override
	public Class<EditContentRestrictionsSectionModel> getModelClass()
	{
		return EditContentRestrictionsSectionModel.class;
	}

	@NonNullByDefault(false)
	public static class EditContentRestrictionsSectionModel
	{
		@Bookmarked(name = "s")
		private String sessionId;
		@Bookmarked(stateful = false)
		private boolean rendered;
		private Label heading;
		private Map<String, Object> errors = Maps.newHashMap();

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

		public Label getHeading()
		{
			return heading;
		}

		public void setHeading(Label heading)
		{
			this.heading = heading;
		}

		public Map<String, Object> getErrors()
		{
			return errors;
		}

		public void setErrors(Map<String, Object> errors)
		{
			this.errors = errors;
		}

		public String getExpressionPretty()
		{
			return expressionPretty;
		}

		public void setExpressionPretty(String expressionPretty)
		{
			this.expressionPretty = expressionPretty;
		}

		private String expressionPretty;
	}

	@Override
	public void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		ContentLayout.setLayout(info, ContentLayout.ONE_COLUMN);
		SelectedQuota selected = loadSession(info);
		
		if( selected.getQuotaIndex() == -1 )
		{
			decorations.setTitle(LABEL_HEADING_ADD);
		}
		else
		{
			decorations.setTitle(LABEL_HEADING_EDIT);
		}
		
		HtmlLinkState linkState = new HtmlLinkState(new SimpleBookmark("access/contentrestrictions.do"));
		linkState.setLabel(TITLE_LABEL);
		linkState.setTitle(BREADCRUMB_TITLE_LABEL);
		linkState.setClickHandler(cancelFunc);
		crumbs.add(linkState);
	}
}
