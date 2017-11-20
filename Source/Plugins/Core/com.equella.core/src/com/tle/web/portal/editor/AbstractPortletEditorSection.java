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

package com.tle.web.portal.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.edge.web.WebConstants;
import com.google.common.collect.Maps;
import com.tle.beans.Language;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.i18n.LocaleUtils;
import com.tle.common.i18n.beans.LanguageBundleBean;
import com.tle.common.portal.PortletConstants;
import com.tle.common.portal.entity.Portlet;
import com.tle.common.recipientselector.formatter.ExpressionFormatter;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.core.i18n.service.LanguageService;
import com.tle.core.portal.service.PortletEditingBean;
import com.tle.core.portal.service.PortletEditingSession;
import com.tle.core.portal.service.PortletService;
import com.tle.core.services.user.UserService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.service.PortletWebService;
import com.tle.web.recipientselector.ExpressionSelectorDialog;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.MultiEditBox;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.events.js.SubmitValuesHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public abstract class AbstractPortletEditorSection<M extends AbstractPortletEditorSection.AbstractPortletEditorModel>
	extends
		AbstractPrototypeSection<M>
	implements HtmlRenderer, PortletEditor
{
	@PlugKey("page.portal.title")
	private static Label BREADCRUMB_DASHBOARD_LABEL;
	@PlugKey("page.breadcrumb.title")
	private static Label BREADCRUMB_DASHBOARD_TITLE;
	@PlugKey("page.admin.title")
	private static Label BREADCRUMB_ADMIN_LABEL;
	@PlugKey("page.admin.breadcrumb.title")
	private static Label BREADCRUMB_ADMIN_TITLE;
	@PlugKey("page.createportlet.title")
	private static Label CREATE_TITLE_LABEL;
	@PlugKey("page.editportlet.title")
	private static Label EDIT_TITLE_LABEL;
	@PlugKey("portlet.editor.expressionselector.title")
	private static Label EXPRESSION_LABEL;
	@PlugKey("editor.label.pagetitlenew")
	private static String CREATE_PAGETITLE_KEY;
	@PlugKey("editor.label.pagetitleedit")
	private static String EDIT_PAGETITLE_KEY;
	@PlugKey("portlet.editor.error.title.mandatory")
	private static String ERROR_TITLE_KEY;

	@Inject
	private PortletService portletService;
	@Inject
	private PortletWebService portletWebService;
	@Inject
	private UserService userService;
	@Inject
	private ComponentFactory componentFactory;
	@Inject
	private LanguageService langService;

	@ViewFactory
	private FreemarkerFactory view;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajaxFactory;

	@Inject
	@Component(name = "t", stateful = false)
	private MultiEditBox title;
	@Component(name = "d", stateful = false)
	private Checkbox disabled;
	@Component(name = "i", stateful = false)
	private Checkbox institutional;
	@Component(name = "c", stateful = false)
	private Checkbox closeable;
	@Component(name = "m", stateful = false)
	private Checkbox minimisable;
	@PlugKey("editor.button.save")
	@Component(name = "sv", stateful = false)
	private Button saveButton;
	@PlugKey("editor.button.cancel")
	@Component(name = "cl", stateful = false)
	private Button cancelButton;

	@Inject
	private ExpressionSelectorDialog selector;

	private HtmlLinkState breadcrumbDashboard;

	private HtmlLinkState breadcrumbAdmin;

	protected abstract Portlet createNewPortlet();

	protected abstract SectionRenderable customRender(RenderEventContext context, M model, PortletEditingBean portlet)
		throws Exception;

	protected abstract void customValidate(SectionInfo info, PortletEditingBean portlet, Map<String, Object> errors);

	protected abstract void customLoad(SectionInfo info, PortletEditingBean portlet);

	protected abstract void customSave(SectionInfo info, PortletEditingBean portlet);

	protected abstract void customClear(SectionInfo info);

	@Override
	public void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		M model = getModel(info);
		PortletEditingBean portlet = model.getPortlet();
		if( portlet.isAdmin() )
		{
			crumbs.add(SettingsUtils.getBreadcrumb());
			crumbs.add(breadcrumbAdmin);
		}
		else
		{
			crumbs.addToStart(breadcrumbDashboard);
		}

		decorations.setTitle(portlet.getId() == 0 ? CREATE_TITLE_LABEL : EDIT_TITLE_LABEL);
		decorations.setContentBodyClass("portletedit");
	}

	@Override
	public final SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final M model = getModel(context);
		final PortletEditingSession session = portletService.loadSession(model.getSessionId());
		final PortletEditingBean portlet = session.getBean();
		model.setPortlet(portlet);
		model.setSession(session);
		final String portletName = CurrentLocale
			.get(portletService.mapAllAvailableTypes().get(portlet.getType()).getNameKey());

		model.setAdmin(portlet.isAdmin());
		model.setErrors(session.getValidationErrors());
		model.setCustomEditor(customRender(context, model, portlet));

		setupInstitutionWideSettings(context);

		LanguageBundleBean titleBundle = title.getLanguageBundle(context);
		if( LangUtils.isEmpty(titleBundle) )
		{
			List<Language> languages = langService.getLanguages();

			if( !Check.isEmpty(languages) )
			{
				List<Locale> locales = new ArrayList<Locale>();
				for( Language lang : languages )
				{
					locales.add(lang.getLocale());
				}
				Collections.sort(locales, new Comparator<Locale>()
				{
					@Override
					public int compare(Locale loc0, Locale loc1)
					{
						return loc0.toString().compareTo(loc1.toString());
					}
				});

				Locale closest = LocaleUtils.getClosestLocale(locales, CurrentLocale.getLocale());
				title.setLanguageBundle(context, LangUtils.convertBundleToBean(
					LangUtils.createTextTempLangugageBundle(portletName, closest != null ? closest : locales.get(0))));
			}
		}

		String expression = selector.getExpression(context);
		if( Check.isEmpty(expression) )
		{
			expression = SecurityConstants.getRecipient(Recipient.OWNER);
		}
		model.setExpressionPretty(new ExpressionFormatter(userService).convertToInfix(expression));
		selector.setExpression(context, expression);

		model.setPageTitle(
			CurrentLocale.get(portlet.getId() == 0 ? CREATE_PAGETITLE_KEY : EDIT_PAGETITLE_KEY, portletName));

		return view.createResult("edit/editcommon.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		final SubmitValuesHandler cancelFunc = events.getNamedHandler("cancel");

		saveButton.setClickHandler(events.getNamedHandler("save"));
		cancelButton.setClickHandler(cancelFunc);

		selector.setOkCallback(events.getSubmitValuesFunction("expression"));

		componentFactory.registerComponent(id, "selector", tree, selector);
		selector.setTitle(EXPRESSION_LABEL);

		institutional
			.setEventHandler(JSHandler.EVENT_CHANGE,
				new StatementHandler(ajaxFactory.getAjaxUpdateDomFunction(tree, this,
					events.getEventHandler("setupInstitutionWideSettings"),
					ajaxFactory.getEffectFunction(EffectType.REPLACE_IN_PLACE), "institutionWideSettings")));

		breadcrumbDashboard = new HtmlLinkState(new SimpleBookmark(WebConstants.DEFAULT_HOME_PAGE));
		breadcrumbDashboard.setLabel(BREADCRUMB_DASHBOARD_LABEL);
		breadcrumbDashboard.setTitle(BREADCRUMB_DASHBOARD_TITLE);

		breadcrumbAdmin = new HtmlLinkState(cancelFunc);
		breadcrumbAdmin.setLabel(BREADCRUMB_ADMIN_LABEL);
		breadcrumbAdmin.setTitle(BREADCRUMB_ADMIN_TITLE);
	}

	@EventHandlerMethod
	public void setupInstitutionWideSettings(SectionInfo info) throws Exception
	{
		getModel(info).setInstitutionWideChecked(institutional.isChecked(info));
	}

	@EventHandlerMethod
	public void expression(SectionInfo info, String selectorId, String expression) throws Exception
	{
		final PortletEditingSession session = portletService.loadSession(getModel(info).getSessionId());
		final PortletEditingBean bean = session.getBean();
		bean.setTargetExpression(expression);
	}

	@EventHandlerMethod
	public final void save(SectionInfo info)
	{
		final M model = getModel(info);
		final PortletEditingSession session = portletService.loadSession(model.getSessionId());
		saveInternal(info, session);
		if( session.isValid() )
		{
			portletService.commitSession(session);
			PortletEditingBean portletBean = session.getBean();
			portletWebService.returnFromEdit(info, false, portletBean.getUuid(), portletBean.isInstitutional());
			model.setSessionId(null);
		}
	}

	private void saveFields(SectionInfo info, EntityPack<Portlet> pack, PortletEditingBean portlet)
	{
		final boolean admin = portletService.canModifyAdminFields();

		portlet.setName(title.getLanguageBundle(info));
		if( admin )
		{
			boolean instWideChecked = institutional.isChecked(info);
			portlet.setInstitutional(instWideChecked);
			portlet.setEnabled(!disabled.isChecked(info));
			portlet.setCloseable(!instWideChecked || closeable.isChecked(info));
			portlet.setMinimisable(!instWideChecked || minimisable.isChecked(info));

			final String expression = selector.getExpression(info);
			if( expression != null )
			{
				final TargetList list = new TargetList();
				list.setEntries(new ArrayList<TargetListEntry>());
				list.getEntries().add(new TargetListEntry(true, false, PortletConstants.VIEW_PORTLET, expression));
				pack.setTargetList(list);
			}
		}

		customSave(info, portlet);
	}

	@EventHandlerMethod
	public final void cancel(SectionInfo info)
	{
		final M model = getModel(info);
		portletService.cancelSessionId(model.getSessionId());
		model.setSessionId(null);
		portletWebService.returnFromEdit(info, true, null, false);
	}

	private boolean validate(SectionInfo info, PortletEditingBean portlet, Map<String, Object> errors)
	{
		errors.clear();
		LanguageBundleBean bundle = title.getLanguageBundle(info);
		if( LangUtils.isEmpty(bundle) )
		{
			errors.put("title", CurrentLocale.get(ERROR_TITLE_KEY));
		}
		customValidate(info, portlet, errors);
		return errors.isEmpty();
	}

	@Override
	public void saveToSession(SectionInfo info)
	{
		final PortletEditingSession session = portletService.loadSession(getModel(info).getSessionId());
		saveInternal(info, session);
	}

	private PortletEditingSession saveInternal(SectionInfo info, PortletEditingSession session)
	{
		final EntityPack<Portlet> pack = session.getPack();

		final PortletEditingBean bean = session.getBean();
		session.setValid(validate(info, bean, session.getValidationErrors()));

		// Save fields even if invalid, it wont be committed until the session
		// is valid
		saveFields(info, pack, bean);

		portletService.saveSession(session);
		return session;
	}

	@Override
	public void create(SectionInfo info, String type, boolean admin)
	{
		final PortletEditingSession session = portletService.startNewSession(createNewPortlet());
		final PortletEditingBean bean = session.getBean();
		bean.setAdmin(admin);
		startSession(info, session);
	}

	@Override
	public void edit(SectionInfo info, String portletUuid, boolean admin)
	{
		final PortletEditingSession session = portletService.startEditingSession(portletUuid);
		final PortletEditingBean bean = session.getBean();
		bean.setAdmin(admin);
		startSession(info, session);
	}

	private void startSession(SectionInfo info, PortletEditingSession session)
	{
		loadInternal(info, session);
		getModel(info).setSessionId(session.getSessionId());
	}

	@Override
	public void loadFromSession(SectionInfo info)
	{
		final String sessionId = getModel(info).getSessionId();
		if( sessionId != null )
		{
			final PortletEditingSession session = portletService.loadSession(sessionId);
			loadInternal(info, session);
		}
	}

	private void loadInternal(SectionInfo info, PortletEditingSession session)
	{
		final PortletEditingBean bean = session.getBean();

		LanguageBundleBean name = bean.getName();
		if( name != null )
		{
			title.setLanguageBundle(info, name);
		}
		final String expression = bean.getTargetExpression();
		if( expression != null )
		{
			selector.setExpression(info, expression);
		}

		disabled.setChecked(info, !bean.isEnabled());
		institutional.setChecked(info, bean.isInstitutional());
		closeable.setChecked(info, bean.isCloseable());
		minimisable.setChecked(info, bean.isMinimisable());

		customLoad(info, bean);
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerSections(this, parentId);
	}

	@Override
	public SectionRenderable render(RenderContext info)
	{
		return renderSection(info, this);
	}

	@Override
	public void restore(SectionInfo info)
	{
		// Nothing by default
	}

	public MultiEditBox getTitle()
	{
		return title;
	}

	public Checkbox getDisabled()
	{
		return disabled;
	}

	public Checkbox getInstitutional()
	{
		return institutional;
	}

	public Checkbox getCloseable()
	{
		return closeable;
	}

	public Checkbox getMinimisable()
	{
		return minimisable;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public ExpressionSelectorDialog getSelector()
	{
		return selector;
	}

	public static class AbstractPortletEditorModel
	{
		@Bookmarked(name = "s")
		private String sessionId;

		private String pageTitle;
		private String expressionPretty;
		private boolean admin;
		private SectionRenderable customEditor;
		private Map<String, Object> errors = Maps.newHashMap();
		private boolean institutionWideChecked;
		private PortletEditingSession session;
		private PortletEditingBean portlet;

		public String getSessionId()
		{
			return sessionId;
		}

		public void setSessionId(String sessionId)
		{
			this.sessionId = sessionId;
		}

		public String getPageTitle()
		{
			return pageTitle;
		}

		public void setPageTitle(String pageTitle)
		{
			this.pageTitle = pageTitle;
		}

		public String getExpressionPretty()
		{
			return expressionPretty;
		}

		public void setExpressionPretty(String expressionPretty)
		{
			this.expressionPretty = expressionPretty;
		}

		public boolean isAdmin()
		{
			return admin;
		}

		public void setAdmin(boolean admin)
		{
			this.admin = admin;
		}

		public SectionRenderable getCustomEditor()
		{
			return customEditor;
		}

		public void setCustomEditor(SectionRenderable customEditor)
		{
			this.customEditor = customEditor;
		}

		public Map<String, Object> getErrors()
		{
			return errors;
		}

		public void setErrors(Map<String, Object> errors)
		{
			this.errors = errors;
		}

		public boolean isInstitutionWideChecked()
		{
			return institutionWideChecked;
		}

		public void setInstitutionWideChecked(boolean institutionWideChecked)
		{
			this.institutionWideChecked = institutionWideChecked;
		}

		public PortletEditingSession getSession()
		{
			return session;
		}

		public void setSession(PortletEditingSession session)
		{
			this.session = session;
		}

		public PortletEditingBean getPortlet()
		{
			return portlet;
		}

		public void setPortlet(PortletEditingBean portlet)
		{
			this.portlet = portlet;
		}
	}
}
