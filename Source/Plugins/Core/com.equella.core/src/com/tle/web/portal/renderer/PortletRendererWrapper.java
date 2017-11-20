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

package com.tle.web.portal.renderer;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.portal.entity.Portlet;
import com.tle.common.portal.entity.PortletPreference;
import com.tle.core.accessibility.AccessibilityModeService;
import com.tle.core.guice.Bind;
import com.tle.core.portal.service.PortletService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.section.enduser.RootPortletSection;
import com.tle.web.portal.service.PortletWebService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.component.Box;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.ReloadFunction;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
public class PortletRendererWrapper
	extends
		AbstractPrototypeSection<PortletRendererWrapper.PortletRendererWrapperModel> implements HtmlRenderer
{
	private static final PluginResourceHelper RESOURCES = ResourcesService.getResourceHelper(RootPortletSection.class);

	private static final IncludeFile INCLUDE = new IncludeFile(RESOURCES.url("scripts/portal.js"));
	private static final JSCallable EDIT_FUNC = new ExternallyDefinedFunction("editPortlet", INCLUDE);

	@Inject
	private PortletService portletService;
	@Inject
	private PortletWebService portletWebService;
	@Inject
	private AccessibilityModeService accessibilityService;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Component(register = false)
	private Box box;

	private String portletUuid;
	private boolean minimised;
	private PortletContentRenderer<?> delegate;

	@Override
	public final SectionResult renderHtml(RenderEventContext context)
	{
		final PortletRendererWrapperModel model = getModel(context);
		final Portlet portlet = portletService.getByUuid(portletUuid);

		if( !delegate.canView(context) )
		{
			return null;
		}
		// FIXME: haxical
		if( delegate instanceof PreRenderable )
		{
			((PreRenderable) delegate).preRender(context.getPreRenderContext());
		}

		model.setUuid(portlet.getUuid());
		model.setType(portlet.getType());
		PortletPreference pref = portletService.getPreference(portlet);
		if( pref == null )
		{
			model.setStyle("wide");
		}
		else
		{
			model.setStyle(pref.getPosition() == PortletPreference.POSITION_TOP ? "wide" : "normal");
		}

		box.setMinimised(context, (minimised && !accessibilityService.isAccessibilityMode()));
		// box.setDraggable(context, !CurrentUser.wasAutoLoggedIn());
		if( !minimised || accessibilityService.isAccessibilityMode() )
		{
			model.setContent(renderFirstResult(context));
		}

		return viewFactory.createResult("portlet/portlettemplate.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		final Portlet portlet = portletService.getByUuid(portletUuid);

		final String updateId = "portlet_" + portletUuid;
		box.setPreferredId(updateId);
		tree.registerInnerSection(box, id);

		box.setLabel(new TextLabel(CurrentLocale.get(portlet.getName())));

		final boolean allowChanges = !CurrentUser.wasAutoLoggedIn() && !CurrentUser.isGuest();

		if( allowChanges && portlet.isMinimisable() )
		{
			box.addMinimiseHandler(events.getEventHandler("minimisePortlet"));
		}

		if( allowChanges && portlet.isCloseable() )
		{
			final boolean owner = portlet.getOwner().equals(CurrentUser.getUserID());
			if( (owner && portletService.canDelete(portlet)) || portlet.isInstitutional() )
			{
				final String op = (owner && !portlet.isInstitutional() ? "delete" : "close");
				box.addCloseHandler(new OverrideHandler(events.getNamedHandler("closePortlet"))
					.addValidator(new Confirm(new KeyLabel(RESOURCES.key("portlet.confirm." + op)))));
			}
		}

		if( allowChanges && portletService.canEdit(portlet) && !portlet.isInstitutional() )
		{
			box.addEditHandler(new StatementHandler(EDIT_FUNC, new SubmitValuesFunction(events
				.getEventHandler("editPortlet"))));
		}
		box.setNoMinMaxOnHeader(true);

		// Add the portal content as a child section
		tree.registerSections(delegate, id);
	}

	protected JSCallable getResultUpdater(SectionTree tree, ParameterizedEvent eventHandler)
	{
		if( eventHandler == null )
		{
			return new ReloadFunction(true);
		}
		return new SubmitValuesFunction(eventHandler);
	}

	@EventHandlerMethod
	public void minimisePortlet(SectionInfo info)
	{
		minimised = !minimised;
		portletWebService.minimise(info, portletUuid, minimised);
	}

	@EventHandlerMethod
	public void editPortlet(SectionInfo info)
	{
		portletWebService.editPortlet(info, portletUuid, false);
	}

	@EventHandlerMethod
	public void closePortlet(SectionInfo info)
	{
		portletWebService.close(info, portletUuid);
	}

	public void setPortletUuid(String portletUuid)
	{
		this.portletUuid = portletUuid;
	}

	public void setMinimised(boolean minimised)
	{
		this.minimised = minimised;
	}

	public void setDelegate(PortletContentRenderer<?> delegate)
	{
		this.delegate = delegate;
	}

	public Box getBox()
	{
		return box;
	}

	public static class PortletRendererWrapperModel
	{
		private SectionResult content;
		private String uuid;
		private String type;
		private String style;

		public String getStyle()
		{
			return style;
		}

		public void setStyle(String style)
		{
			this.style = style;
		}

		public SectionResult getContent()
		{
			return content;
		}

		public void setContent(SectionResult content)
		{
			this.content = content;
		}

		public String getUuid()
		{
			return uuid;
		}

		public void setUuid(String uuid)
		{
			this.uuid = uuid;
		}

		public String getType()
		{
			return type;
		}

		public void setType(String type)
		{
			this.type = type;
		}
	}

	@Override
	public Class<PortletRendererWrapperModel> getModelClass()
	{
		return PortletRendererWrapperModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		String ddpn = delegate.getDefaultPropertyName();
		if( !Check.isEmpty(ddpn) )
		{
			return "wrap" + ddpn;
		}
		return super.getDefaultPropertyName();
	}
}