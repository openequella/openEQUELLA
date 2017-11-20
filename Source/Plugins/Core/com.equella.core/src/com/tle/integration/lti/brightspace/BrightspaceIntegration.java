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

package com.tle.integration.lti.brightspace;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ViewableItemType;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.NameValue;
import com.tle.common.URLUtils;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.connectors.brightspace.BrightspaceConnectorConstants;
import com.tle.core.connectors.brightspace.beans.BrightspaceQuicklink;
import com.tle.core.connectors.brightspace.service.BrightspaceConnectorService;
import com.tle.core.connectors.brightspace.service.BrightspaceConnectorService.TopicCreationOption;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.web.integration.AbstractIntegrationService;
import com.tle.web.integration.IntegrationActionInfo;
import com.tle.web.integration.SingleSignonForm;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.lti.LtiData;
import com.tle.web.lti.usermanagement.LtiUserState;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.RootSelectionSection;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.ViewableItemResolver;

@Bind
@Singleton
@NonNullByDefault
@SuppressWarnings("nls")
public class BrightspaceIntegration extends AbstractIntegrationService<BrightspaceSessionData>
{
	static
	{
		PluginResourceHandler.init(BrightspaceIntegration.class);
	}

	@PlugKey("integration.receipt.addedtobrightspace")
	private static String KEY_RECEIPT_ADDED;
	@PlugKey("integration.error.requireoneconnector")
	private static String KEY_ERROR_NO_SINGLE_CONNECTOR;

	@PlugKey("integration.error.nocourse")
	private static Label LABEL_ERROR_NO_COURSE;
	@PlugKey("integration.error.noapidomain")
	private static Label LABEL_ERROR_NO_API_DOMAIN;

	@Inject
	private IntegrationService integrationService;
	@Inject
	private ViewableItemResolver viewableItemResolver;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private ConnectorService connectorService;
	@Inject
	private BrightspaceConnectorService brightspaceService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private MimeTypeService mimeTypeService;

	@Override
	protected String getIntegrationType()
	{
		return "brightspace";
	}

	public boolean isItemOnly(BrightspaceSessionData data)
	{
		return false;
	}

	@Override
	public BrightspaceSessionData createDataForViewing(SectionInfo info)
	{
		return new BrightspaceSessionData();
	}

	@Override
	public void setupSingleSignOn(SectionInfo info, SingleSignonForm form)
	{
		final BrightspaceSessionData data = new BrightspaceSessionData(info.getRequest(), form.getAction());

		String courseId = null;
		final UserState userState = CurrentUser.getUserState();
		String courseCode = form.getCourseCode();
		if( userState instanceof LtiUserState )
		{
			final LtiUserState ltiUserState = (LtiUserState) userState;
			final LtiData ltiData = ltiUserState.getData();
			if( ltiData != null )
			{
				courseId = form.getCourseId();
				if( Strings.isNullOrEmpty(courseId) )
				{
					courseId = ltiData.getContextId();
				}
				data.setCourseId(courseId);
				data.setContextTitle(ltiData.getContextTitle());
				if( Strings.isNullOrEmpty(courseCode) )
				{
					courseCode = ltiData.getContextLabel();
				}
			}
		}

		data.setCourseInfoCode(integrationService.getCourseInfoCode(courseId, courseCode));

		String formDataAction = form.getAction();
		if( formDataAction == null )
		{
			formDataAction = "searchThin";
		}

		IntegrationActionInfo actionInfo = integrationService.getActionInfo(formDataAction, form.getOptions());
		if( actionInfo.getName().equals("unknown") )
		{
			actionInfo = integrationService.getActionInfoForUrl('/' + data.getAction());
		}
		if( actionInfo == null )
		{
			actionInfo = new IntegrationActionInfo();
		}
		form.setCancelDisabled(true);

		integrationService.standardForward(info, convertToForward(actionInfo, form), data, actionInfo, form);
	}

	private String convertToForward(IntegrationActionInfo action, SingleSignonForm model)
	{
		String forward = action.getPath();
		if( forward == null )
		{
			forward = action.getName();
		}

		if( action.getName().equals("standard") )
		{
			forward = forward + model.getQuery();
		}

		return forward.substring(1);
	}

	@Nullable
	@Override
	public SelectionSession setupSelectionSession(SectionInfo info, BrightspaceSessionData data,
		SelectionSession session, SingleSignonForm form)
	{
		final boolean structured = "structured".equals(data.getAction());

		// No multiple selections for anything but structured.
		session.setSelectMultiple(structured);
		session.setAttachmentUuidUrls(true);
		session.setInitialItemXml(form.getItemXml());
		session.setInitialPowerXml(form.getPowerXml());
		session.setCancelDisabled(form.isCancelDisabled());

		final SelectionSession s = super.setupSelectionSession(info, data, session, form);
		if( s != null )
		{
			s.setAttribute(BrightspaceSignon.KEY_SESSION_TYPE, info.getAttribute(BrightspaceSignon.KEY_SESSION_TYPE));
		}

		return s;
	}

	@Override
	public void forward(SectionInfo info, BrightspaceSessionData data, SectionInfo forward)
	{
		final Connector connector = findConnector(data);
		if( brightspaceService.isRequiresAuthentication(connector) )
		{
			String fwdUrl = new InfoBookmark(forward).getHref();

			if( "structured".equals(data.getAction()) )
			{
				String sessionId = null;
				final RootSelectionSection root = info.lookupSection(RootSelectionSection.class);
				if( root != null )
				{
					sessionId = root.getSessionId(forward);
				}

				final StringBuilder qs = new StringBuilder();
				qs.append("?courseId=").append(URLUtils.urlEncode(data.getCourseId()));
				qs.append("&connectorUuid=").append(URLUtils.urlEncode(findConnector(data).getUuid()));
				if( sessionId != null )
				{
					qs.append("&sessionId=").append(sessionId);
				}
				qs.append("&fwd=").append(URLUtils.urlEncode(fwdUrl));

				final String launchPresentationReturnUrl = data.getLaunchPresentationReturnUrl();
				if( launchPresentationReturnUrl != null && launchPresentationReturnUrl.contains("parentNode") )
				{
					final URI uri = URI.create(launchPresentationReturnUrl);
					final Map<String, String> lpQueryString = URLUtils.parseQueryString(uri.getQuery(), true);
					final String parentNode = lpQueryString.get("parentNode");
					if( parentNode != null )
					{
						qs.append("&selected=").append(parentNode);
					}
				}

				fwdUrl = institutionService.institutionalise("brightspacestructureinit") + qs.toString();
			}

			//forward to d2l auth screen
			String authUrl = brightspaceService.getAuthorisationUrl(connector, fwdUrl, null);
			info.forwardToUrl(authUrl);
		}
		else
		{
			info.forward(forward);
		}
	}

	private Connector findConnector(BrightspaceSessionData data)
	{
		Connector connector = null;
		final String connectorUuid = data.getConnectorUuid();
		if( connectorUuid != null )
		{
			connector = connectorService.getByUuid(connectorUuid);
		}
		if( connector == null )
		{
			// There can only be one connector per institution, there is no other way of determining which to use.
			final List<Connector> connectors = connectorService.enumerateEnabled();
			for( Connector c : connectors )
			{
				if( c.getLmsType().equals(BrightspaceConnectorConstants.CONNECTOR_TYPE) )
				{
					connector = c;
					break;
				}
			}
		}
		if( connector == null )
		{
			throw new RuntimeException();
		}
		return connector;
	}

	@Override
	public boolean select(SectionInfo info, BrightspaceSessionData data, SelectionSession session)
	{
		try
		{
			final Connector connector = findConnector(data);
			final String sessionType = session.getAttribute(BrightspaceSignon.KEY_SESSION_TYPE);

			if( !session.isSelectMultiple() )
			{
				final SelectedResource resource = getFirstSelectedResource(session);
				final IItem<?> item = getItemForResource(resource);

				final String courseId = data.getCourseId();

				final LmsLinkInfo linkInfo = getLinkForResource(info, createViewableItem(item, resource), resource,
					false, session.isAttachmentUuidUrls());
				final LmsLink link = linkInfo.getLmsLink();

				final boolean insertStuff = BrightspaceSignon.SESSION_TYPE_INSERTSTUFF.equals(sessionType);

				final String launchPresentationReturnUrl = data.getLaunchPresentationReturnUrl();
				final String nakedUrl = URLUtils.decompose(launchPresentationReturnUrl)[0];

				final String finalUrl;
				if( insertStuff )
				{
					// INSERT STUFF PLUGIN

					// TODO: Could possibly adapt the MIME type templates.

					// If the selected attachment is an image, we'll assume it's public and the user wants to embed it, otherwise we need an iframe
					// to enable the LTI launch.
					String markup = null;

					// Might be a cloud attachment..
					final IAttachment attachment = linkInfo.getResourceAttachment();
					if( attachment != null && attachment.getAttachmentType() == AttachmentType.FILE )
					{
						final String mimeType = mimeTypeService.getMimeEntryForAttachment((Attachment) attachment);
						if( mimeType != null )
						{
							if( mimeType.startsWith("image/") )
							{
								markup = "<img src=\"" + link.getUrl() + "\" alt=\"" + link.getName() + "\">";
							}
						}
					}

					if( markup == null )
					{
						markup = linkMarkup(connector, courseId, link);
					}

					finalUrl = nakedUrl + "?content=" + URLUtils.urlEncode(markup);
				}
				else
				{
					// QUICKLINK PLUGIN 

					// Null module ID here is OK, since we aren't creating a topic and the redirection in the Brightspace UI handles that.
					final BrightspaceQuicklink ql = brightspaceService.addQuicklink(connector, courseId, null, link,
						TopicCreationOption.NONE);
					finalUrl = nakedUrl + "?quicklink=" + URLUtils.urlEncode(ql.getPublicUrl()) + "&title="
						+ URLUtils.urlEncode(resource.getTitle()) + "&target=NewWindow";
				}

				session.clearResources();
				info.forwardToUrl(finalUrl);
			}
			else
			{
				final String courseId = session.getStructure().getId();

				// add resources via REST
				final Collection<SelectedResource> selectedResources = session.getSelectedResources();
				for( SelectedResource resource : selectedResources )
				{
					final IItem<?> item = getItemForResource(resource);
					final String moduleId = resource.getKey().getFolderId();

					final LmsLink link = getLinkForResource(info, createViewableItem(item, resource), resource, false,
						session.isAttachmentUuidUrls()).getLmsLink();
					brightspaceService.addQuicklink(connector, courseId, moduleId, link, TopicCreationOption.CREATE);

				}
				final int count = selectedResources.size();

				session.clearResources();
				if( BrightspaceSignon.SESSION_TYPE_COURSEBUILDER.equals(sessionType) )
				{
					final String launchPresentationReturnUrl = data.getLaunchPresentationReturnUrl();
					info.forwardToUrl(launchPresentationReturnUrl);
				}
				else
				{
					// provide receipt and stay where we are
					receiptService.setReceipt(new PluralKeyLabel(KEY_RECEIPT_ADDED, count));
				}
			}

			return false;
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	//	private String iframeMarkup(Connector connector, String courseId, LmsLink link)
	//	{
	//		return "<iframe src=\"" + mangledQuicklink(connector, courseId, link, true)
	//			+ "\" width=\"1024\" height=\"800\"></iframe>";
	//	}

	private String linkMarkup(Connector connector, String courseId, LmsLink link)
	{
		return "<a href=\"" + mangledQuicklink(connector, courseId, link, true) + "\" target=\"_blank\">"
			+ link.getName() + "</a>";
	}

	/**
	 * Used for Insert Stuff plugin
	 * 
	 * @param connector
	 * @param courseId
	 * @param link
	 * @return
	 */
	private String mangledQuicklink(Connector connector, String courseId, LmsLink link, boolean sameFrame)
	{
		// Null module ID here is OK, since we aren't creating a topic (we're embedding in HTML editor)
		final BrightspaceQuicklink ql = brightspaceService.addQuicklink(connector, courseId, null, link,
			TopicCreationOption.NONE);
		// Mangle the public URL as per Brightspace docs https://community.brightspace.com/devcop/blog/insert_stuff_with_lti_content
		final String[] decomposedPublicUrl = URLUtils.decompose(ql.getPublicUrl());
		final Map<String, String> publicUrlQs = URLUtils.parseQueryString(decomposedPublicUrl[1], true);
		publicUrlQs.put("srcou", "1");
		String publicUrl = URLUtils.appendQueryString(decomposedPublicUrl[0], URLUtils.getParameterString(publicUrlQs));

		// A bit dirty.  Brightspace expects { and } to be unencoded since it's only really a placeholder, not a real value.
		publicUrl = publicUrl.replace("%7B", "{");
		publicUrl = publicUrl.replace("%7D", "}");
		return publicUrl; /* + "&target=" + (sameFrame ? "SameFrame" : "NewWindow");*/
	}

	@Override
	public String getClose(BrightspaceSessionData data)
	{
		return data.getLaunchPresentationReturnUrl();
	}

	@Nullable
	@Override
	public String getCourseInfoCode(BrightspaceSessionData data)
	{
		return data.getCourseInfoCode();
	}

	@Nullable
	@Override
	public NameValue getLocation(BrightspaceSessionData data)
	{
		return null;
	}

	@Override
	protected boolean canSelect(BrightspaceSessionData data)
	{
		return true;
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
		@Nullable String itemExtensionType)
	{
		final ViewableItem<I> vitem = viewableItemResolver.createIntegrationViewableItem(itemId, latest,
			ViewableItemType.GENERIC, itemExtensionType);
		return vitem;
	}
}
