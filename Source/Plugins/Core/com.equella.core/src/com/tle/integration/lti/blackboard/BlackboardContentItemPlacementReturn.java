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

package com.tle.integration.lti.blackboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Throwables;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemResolver;
import com.tle.core.lti.consumers.service.LtiConsumerService;
import com.tle.integration.lti.LtiSessionData;
import com.tle.web.integration.Integration.LmsLink;
import com.tle.web.integration.IntegrationInterface;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.lti.LtiData;
import com.tle.web.lti.usermanagement.LtiUserState;
import com.tle.web.oauth.service.OAuthWebService;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.header.FormTag;
import com.tle.web.sections.header.SimpleFormAction;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.render.HiddenInput;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.template.Decorations;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

//TODO: we should be able to make this a generic LTI return
@Bind
public class BlackboardContentItemPlacementReturn extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	private static final Logger LOGGER = Logger.getLogger(BlackboardContentItemPlacementReturn.class);

	@Inject
	private SelectionService selectionService;
	@Inject
	private IntegrationService integrationService;
	@Inject
	private BlackboardLtiIntegration blackboardLtiIntegration;
	@Inject
	private ItemResolver itemResolver;
	@Inject
	private OAuthWebService oauthWebService;
	@Inject
	private LtiConsumerService consumerService;

	private static final ObjectMapper mapper = new ObjectMapper();

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{

		final SelectionSession session = selectionService.getCurrentSession(context);
		Decorations.getDecorations(context).clearAllDecorations();

		final SelectedResource resource = session.getSelectedResources().iterator().next();
		final IItem<?> item = getItemForResource(resource);

		final LmsLink link = blackboardLtiIntegration.getLinkForResource(context,
			blackboardLtiIntegration.createViewableItem(item, resource), resource, false, session.isAttachmentUuidUrls())
			.getLmsLink();

		final IntegrationInterface integ = integrationService.getIntegrationInterface(context);
		final LtiSessionData data = (LtiSessionData) integ.getData();

		final String launchUrl = data.getContentItemReturnUrl();

		final Map<String, String[]> formParams = new TreeMap<>();
		addParameter(formParams,"lti_message_type", "ContentItemSelection");
		addParameter(formParams,"lti_version", "LTI-1p0");
		addParameter(formParams,"data", data.getData());
		addParameter(formParams,"content_items", buildSelectionJson(link));

		final FormTag formTag = context.getForm();
		formTag.setName("ltiLaunchForm");
		formTag.setElementId(new SimpleElementId("ltiLaunchForm"));
		formTag.setAction(new SimpleFormAction(launchUrl));
		formTag.setEncoding("application/x-www-form-urlencoded");
		formTag.setMethod("POST");

		final List<Map.Entry<String, String>> finalParams = signParameters(launchUrl, formParams);
		for( Entry<String, String> param : finalParams )
		{
			final String val = param.getValue();
			if( !Check.isEmpty(val) )
			{
				formTag.addHidden(new HiddenInput(param.getKey(), val));
			}
		}

		formTag.addReadyStatements(
			Js.statement(Js.methodCall(Jq.$('#' + formTag.getElementId(context)), Js.function("submit"))));
		return null;
	}

	private void addParameter(Map<String, String[]> params, String key, String value)
	{
		params.put(key, new String[]{ value });
	}

	private String buildSelectionJson(LmsLink link)
	{
		final ContentItemSelection selection = new ContentItemSelection();
		selection.context = "http://purl.imsglobal.org/ctx/lti/v1/ContentItem";
		final ContentItemSelection.ContentItemGraph graph = new ContentItemSelection.ContentItemGraph();
		graph.type = "LtiLinkItem";
		graph.id = link.getUrl();
		graph.url = link.getUrl();
		graph.title = link.getName();
		graph.text = link.getName();
		graph.mediaType = "application/vnd.ims.lti.v1.ltilink";
		graph.windowTarget = "_blank";
		final ContentItemSelection.ContentItemGraph.ContentItemPlacementAdvice placementAdvice = new ContentItemSelection.ContentItemGraph.ContentItemPlacementAdvice();
		placementAdvice.presentationDocumentTarget = "window";
		graph.placementAdvice = placementAdvice;
		final List<ContentItemSelection.ContentItemGraph> graphList = new ArrayList<>();
		graphList.add(graph);
		selection.graph = graphList;

		try
		{
			return mapper.writeValueAsString(selection);
		}
		catch (JsonProcessingException ex)
		{
			throw Throwables.propagate(ex);
		}
	}

	private List<Map.Entry<String, String>> signParameters(String launchUrl, Map<String, String[]> formParams)
	{
		final LtiData.OAuthData oauthData = getOAuthData();
		if (oauthData == null)
		{
			throw new RuntimeException("Not currently in an LTI session");
		}

		return oauthWebService.getOauthSignatureParams(oauthData.getConsumerKey(), oauthData.getConsumerSecret(), launchUrl, formParams);
	}

	private LtiData.OAuthData getOAuthData()
	{
		final UserState userState = CurrentUser.getUserState();
		if( userState instanceof LtiUserState)
		{
			final LtiData ltiData = ((LtiUserState) userState).getData();
			if ( ltiData != null )
			{
				return ltiData.getOAuthData();
			}
		}
		return null;
	}

	private IItem<?> getItemForResource(SelectedResource resource)
	{
		final String uuid = resource.getUuid();
		final String extensionType = resource.getKey().getExtensionType();
		final ItemId itemId;
		if( resource.isLatest() )
		{
			final int latestVersion = itemResolver.getLiveItemVersion(uuid, extensionType);
			itemId = new ItemId(uuid, latestVersion);
		}
		else
		{
			itemId = new ItemId(uuid, resource.getVersion());
		}
		final IItem<?> item = itemResolver.getItem(itemId, extensionType);
		if( item == null )
		{
			throw new RuntimeException(CurrentLocale.get("com.tle.web.integration.error.noitemforresource",
				resource.getUuid(), resource.getVersion()));
		}
		return item;
	}

	//TODO: move to generic LTI area
	public static class ContentItemSelection
	{
		@JsonProperty("@context")
		private String context;
		@JsonProperty("@graph")
		private List<ContentItemGraph> graph;

		public String getContext()
		{
			return context;
		}

		public void setContext(String context)
		{
			this.context = context;
		}

		public List<ContentItemGraph> getGraph()
		{
			return graph;
		}

		public void setGraph(List<ContentItemGraph> graph)
		{
			this.graph = graph;
		}

		public static class ContentItemGraph
		{
			@JsonProperty("@type")
			private String type;
			@JsonProperty("@id")
			private String id;
			private String url;
			private String title;
			private String text;
			private String mediaType;
			private String windowTarget;
			private ContentItemPlacementAdvice placementAdvice;

			public String getType()
			{
				return type;
			}

			public void setType(String type)
			{
				this.type = type;
			}

			public String getId()
			{
				return id;
			}

			public void setId(String id)
			{
				this.id = id;
			}

			public String getUrl()
			{
				return url;
			}

			public void setUrl(String url)
			{
				this.url = url;
			}

			public String getTitle()
			{
				return title;
			}

			public void setTitle(String title)
			{
				this.title = title;
			}

			public String getText()
			{
				return text;
			}

			public void setText(String text)
			{
				this.text = text;
			}

			public String getMediaType()
			{
				return mediaType;
			}

			public void setMediaType(String mediaType)
			{
				this.mediaType = mediaType;
			}

			public String getWindowTarget()
			{
				return windowTarget;
			}

			public void setWindowTarget(String windowTarget)
			{
				this.windowTarget = windowTarget;
			}

			public ContentItemPlacementAdvice getPlacementAdvice()
			{
				return placementAdvice;
			}

			public void setPlacementAdvice(ContentItemPlacementAdvice placementAdvice)
			{
				this.placementAdvice = placementAdvice;
			}

			public static class ContentItemPlacementAdvice
			{
				private String presentationDocumentTarget;

				public String getPresentationDocumentTarget()
				{
					return presentationDocumentTarget;
				}

				public void setPresentationDocumentTarget(String presentationDocumentTarget)
				{
					this.presentationDocumentTarget = presentationDocumentTarget;
				}
			}
		}
	}
}
