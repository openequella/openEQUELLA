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

package com.tle.web.integration.generic;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ViewableItemType;
import com.tle.common.NameValue;
import com.tle.common.URLUtils;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.jackson.mapper.JaxbMapperExtension;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.web.integration.AbstractIntegrationService;
import com.tle.web.integration.IntegrationSessionData;
import com.tle.web.integration.IntegrationSessionExtension;
import com.tle.web.integration.SingleSignonForm;
import com.tle.web.lti.LtiData;
import com.tle.web.lti.usermanagement.LtiUserState;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.header.SimpleFormAction;
import com.tle.web.sections.js.generic.statement.ReloadStatement;
import com.tle.web.sections.render.HiddenInput;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.RootSelectionException;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.ViewableItemResolver;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind
@Singleton
public class GenericIntegrationService
	extends
		AbstractIntegrationService<GenericIntegrationService.GenericIntegrationData>
{
	@Inject
	private ViewableItemResolver viewableItemResolver;
	@Inject
	private ObjectMapperService objectMapperService;

	@Override
	protected String getIntegrationType()
	{
		return "generic";
	}

	@Override
	public boolean select(SectionInfo info, final GenericIntegrationData data, SelectionSession session)
	{
		if( session.isSelectMultiple() || session.isForcePost() )
		{
			final Collection<SelectedResource> selectedResources = session.getSelectedResources();
			final Set<IntegrationSessionExtension> extensions = getExtensions();
			final ObjectMapper mapper = objectMapperService.createObjectMapper(JaxbMapperExtension.NAME);
			final ArrayNode resources = mapper.createArrayNode();
			for( SelectedResource resource : selectedResources )
			{
				IItem<?> item = getItemForResource(resource);
				LmsLink link = getLinkForResource(info, createViewableItem(item, resource), resource, false,
					session.isAttachmentUuidUrls()).getLmsLink();
				ObjectNode jsonLink = mapper.valueToTree(link);

				for( IntegrationSessionExtension extension : extensions )
				{
					extension.processResultForMultiple(info, session, jsonLink, item, resource);
				}
				resources.add(jsonLink);
			}

			RenderContext renderContext = info.getRootRenderContext();
			renderContext.getForm().setAction(new SimpleFormAction(data.getCallbackURL()));

			try
			{
				DivRenderer divTag = new DivRenderer(
					new HiddenInput(data.getPrefix() + "links", mapper.writeValueAsString(resources)));
				divTag.getTagState().addReadyStatements(new ReloadStatement());
				renderContext.setRenderedBody(divTag);
			}
			catch( IOException json )
			{
				throw Throwables.propagate(json);
			}
		}
		else
		{
			SelectedResource resource = getFirstSelectedResource(session);
			IItem<?> item = getItemForResource(resource);
			LmsLink link = getLinkForResource(info, createViewableItem(item, resource), resource, false,
				session.isAttachmentUuidUrls()).getLmsLink();
			forwardToLms(info, session, data, item, getTypeForResource(data, resource, link), link, resource);
		}

		return false;
	}

	@Override
	public GenericIntegrationData createDataForViewing(SectionInfo info)
	{
		return new GenericIntegrationData("view", null, null, null, null, null, false);
	}

	public void forwardToLms(SectionInfo info, SelectionSession session, GenericIntegrationData data, IItem<?> item,
		String type, LmsLink link, SelectedResource resource)
	{
		Map<String, String> params = new LinkedHashMap<String, String>();
		String prefix = data.getPrefix();
		if( prefix == null )
		{
			prefix = "";
		}

		params.put(prefix + "result", "success");
		params.put(prefix + "name", link.getName());
		params.put(prefix + "description", link.getDescription());
		params.put(prefix + "url", link.getUrl());
		params.put(prefix + "type", type);
		params.put(prefix + "uuid", item.getUuid());
		// TODO: dirty
		if( item instanceof Item )
		{
			params.put(prefix + "itemdefuuid", ((Item) item).getItemDefinition().getUuid());
		}
		params.put(prefix + "version", Integer.toString(item.getVersion()));

		for( IntegrationSessionExtension extension : getExtensions() )
		{
			extension.processResultForSingle(info, session, params, prefix, item, resource);
		}

		String callbackURL = data.getCallbackURL();
		String queryParameterString = URLUtils.getParameterString(params);
		String urlstring = callbackURL
			+ (callbackURL.indexOf('?') > 0 ? '&' + queryParameterString : '?' + queryParameterString);
		info.forwardToUrl(urlstring);
	}

	@Nullable
	@Override
	public SelectionSession setupSelectionSession(SectionInfo info, GenericIntegrationData data,
		SelectionSession session, SingleSignonForm form)
	{
		try
		{
			final SelectionSession s = super.setupSelectionSession(info, data, session, form);

			if( s != null )
			{
				s.setSelectMultiple(form.isSelectMultiple());
				s.setForcePost(form.isForcePost());
				s.setUseDownloadPrivilege(form.isUseDownloadPrivilege());
				s.setInitialItemXml(form.getItemXml());
				s.setInitialPowerXml(form.getPowerXml());
				s.setCancelDisabled(form.isCancelDisabled());
				s.setAttachmentUuidUrls(form.isAttachmentUuidUrls());
			}
			return s;
		}
		catch( Exception e )
		{
			// any exceptions to be handled by configured "exceptionHandler"
			throw new RootSelectionException(e);
		}
	}

	@Override
	public String getClose(GenericIntegrationData data)
	{
		// Referrer should be a LAST resort.
		final String cancelUrl = data.getCancelURL();
		if( !Strings.isNullOrEmpty(cancelUrl) )
		{
			return cancelUrl;
		}
		final UserState userState = CurrentUser.getUserState();
		if( userState instanceof LtiUserState )
		{
			final LtiUserState ltiState = (LtiUserState) userState;
			final LtiData ltiData = ltiState.getData();
			final String ltiReturnUrl = ltiData.getReturnUrl();
			if( !Strings.isNullOrEmpty(ltiReturnUrl) )
			{
				return ltiReturnUrl;
			}
		}
		final String cbUrl = data.getCallbackURL();
		if( !Strings.isNullOrEmpty(cbUrl) )
		{
			return cbUrl;
		}
		return data.getReferrer();
	}

	@Override
	public String getCourseInfoCode(GenericIntegrationData data)
	{
		return data.getCourseInfoCode();
	}

	@NonNullByDefault(false)
	public static class GenericIntegrationData implements IntegrationSessionData
	{
		private static final long serialVersionUID = 1L;
		private final String callbackURL;
		private final String cancelURL;
		private final String referrer;
		private final String prefix;
		private final String action;
		private final String template;
		private final boolean forSelection;
		private String courseInfoCode;

		public GenericIntegrationData(String template, String callbackUrl, String cancelUrl, String prefix,
			String referrer, String action, boolean forSelection)
		{
			this.template = template;
			this.callbackURL = dodgyFixUrl(callbackUrl);
			this.cancelURL = dodgyFixUrl(cancelUrl);
			this.prefix = prefix;
			this.referrer = referrer;
			this.action = action;
			this.forSelection = forSelection;
		}

		/**
		 * This code was here before, but has been refactored out. I can't think
		 * of any real world case where the URL would be everything except the
		 * http:// part but I'd better not remove it
		 */
		@SuppressWarnings("unused")
		private String dodgyFixUrl(String url)
		{
			if( url != null )
			{
				try
				{
					new URL(url);
				}
				catch( MalformedURLException mue )
				{
					url = "http://" + url;
				}
				return url;
			}
			return null;
		}

		public String getAction()
		{
			return action;
		}

		public String getCallbackURL()
		{
			return callbackURL;
		}

		public String getCancelURL()
		{
			return cancelURL;
		}

		public String getCourseInfoCode()
		{
			return courseInfoCode;
		}

		public void setCourseInfoCode(String courseInfoCode)
		{
			this.courseInfoCode = courseInfoCode;
		}

		public String getPrefix()
		{
			return prefix;
		}

		public String getReferrer()
		{
			return referrer;
		}

		@Override
		public String getIntegrationType()
		{
			return "gen";
		}

		public String getTemplateName()
		{
			return template;
		}

		@Override
		public boolean isForSelection()
		{
			return forSelection;
		}
	}

	public Class<?> getIntegrationClass()
	{
		return GenericIntegrationService.class;
	}

	@Nullable
	@Override
	public NameValue getLocation(GenericIntegrationData data)
	{
		return null;
	}

	@Override
	protected boolean canSelect(GenericIntegrationData data)
	{
		return data.getCallbackURL() != null;
	}

	protected String getTypeForResource(GenericIntegrationData data, SelectedResource resource, LmsLink link)
	{
		char rtype = resource.getType();
		if( rtype == SelectedResource.TYPE_REMOTE )
		{
			return "link";
		}
		String type = "resource";
		if( rtype == SelectedResource.TYPE_PATH && resource.getUrl().length() == 0 )
		{
			if( data.getAction().equals("searchPlans") )
			{
				type = "plan";
			}
		}
		return type;
	}

	@Override
	protected <I extends IItem<?>> ViewableItem<I> createViewableItem(I item, SelectedResource resource)
	{
		final ViewableItem<I> vitem = viewableItemResolver.createIntegrationViewableItem(item, resource.isLatest(),
			ViewableItemType.GENERIC, resource.getKey().getExtensionType());
		return vitem;
	}

	@Override
	public <I extends IItem<?>> ViewableItem<I> createViewableItem(ItemId itemId, boolean latest,
		String itemExtensionType)
	{
		final ViewableItem<I> vitem = viewableItemResolver.createIntegrationViewableItem(itemId, latest,
			ViewableItemType.GENERIC, itemExtensionType);
		return vitem;
	}

}
