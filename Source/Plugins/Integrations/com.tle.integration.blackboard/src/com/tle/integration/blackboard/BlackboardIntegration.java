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

package com.tle.integration.blackboard;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.dytech.devlib.Base64;
import com.dytech.devlib.PropBagEx;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.tle.annotation.Nullable;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ViewableItemType;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.item.service.ItemService;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.jackson.mapper.JaxbMapperExtension;
import com.tle.core.services.FileSystemService;
import com.tle.integration.blackboard.gateways.BlackboardExport;
import com.tle.web.integration.AbstractIntegrationService;
import com.tle.web.integration.IntegrationActionInfo;
import com.tle.web.integration.IntegrationSessionExtension;
import com.tle.web.integration.SingleSignonForm;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionSession;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.ViewableItemResolver;

/**
 * Note: This is actually UNUSED for the current Building Block. It exists for
 * legacy reasons only. Blackboard now uses the generic integration.
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class BlackboardIntegration extends AbstractIntegrationService<BlackBoardSessionData>
{
	private static final Logger LOGGER = Logger.getLogger(BlackboardIntegration.class);

	@Inject
	private ItemHelper itemHelper;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private ViewableItemResolver viewableItemResolver;
	@Inject
	private ObjectMapperService objectMapperService;
	@Inject
	private ItemService itemService;
	@Inject
	private ItemFileService itemFileService;
	@Inject
	private IntegrationService integrationService;

	@Override
	protected String getIntegrationType()
	{
		return "blackboard";
	}

	public boolean isItemOnly(BlackBoardSessionData data)
	{
		return false;
	}

	@Override
	public BlackBoardSessionData createDataForViewing(SectionInfo info)
	{
		return new BlackBoardSessionData();
	}

	@Override
	public void setupSingleSignOn(SectionInfo info, SingleSignonForm form)
	{
		final BlackBoardSessionData data = new BlackBoardSessionData(info.getRequest());
		data.setCourseInfoCode(integrationService.getCourseInfoCode(form.getCourseId(), form.getCourseCode()));
		initSession(data);

		IntegrationActionInfo actionInfo = integrationService.getActionInfo(data.getEntryUrl(), form.getOptions());
		if( actionInfo.getName().equals("unknown") )
		{
			actionInfo = integrationService.getActionInfoForUrl('/' + data.getEntryUrl());
		}

		if( actionInfo == null )
		{
			actionInfo = new IntegrationActionInfo();
		}

		integrationService.standardForward(info, data.getEntryUrl(), data, actionInfo, form);
	}

	@Override
	public SelectionSession setupSelectionSession(SectionInfo info, BlackBoardSessionData data,
		SelectionSession session, SingleSignonForm form)
	{
		session.setSelectMultiple(true);
		super.setupSelectionSession(info, data, session, form);
		return session;
	}

	@Override
	public boolean select(SectionInfo info, BlackBoardSessionData data, SelectionSession session)
	{
		try
		{
			if( !session.isSelectMultiple() )
			{
				SelectedResource resource = getFirstSelectedResource(session);
				PropBagEx resultXml = addResourceToBlackboard(info, session, data, resource);

				String referrer = resultXml.getNode("referrer");
				String redirectURL = new URL(new URL(data.getBbUrl()), referrer).toString();
				info.forwardToUrl(redirectURL);
			}
			else
			{
				Collection<SelectedResource> resources = session.getSelectedResources();
				for( SelectedResource resource : resources )
				{
					addResourceToBlackboard(info, session, data, resource);
				}
				info.forwardToUrl(getClose(data));
			}
			return false;
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	private PropBagEx addResourceToBlackboard(SectionInfo info, SelectionSession session, BlackBoardSessionData data,
		SelectedResource resource) throws IOException
	{
		final ItemId itemId = resource.createItemId();
		final LmsLinkInfo linkDetails = getLinkForResource(info, createViewableItem(itemService.get(itemId), resource),
			resource, true, session != null && session.isAttachmentUuidUrls());
		final IItem<?> resourceItem = linkDetails.getResourceItem();

		PropBagEx xml = itemHelper.convertToXml(itemService.getItemPack(resourceItem.getItemId()));
		PropBagEx itemxml = xml.aquireSubtree("item");
		if( resource.isLatest() )
		{
			itemxml.setNode("@version", 0);
		}

		ActivateRequest arequest = resource.getAttribute("ActivateRequest");
		if( arequest != null )
		{
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			itemxml.setNode("requestUuid", arequest.getUuid());
			itemxml.setNode("description", arequest.getDescription());
			itemxml.setNode("after", format.format(arequest.getFrom()));
			itemxml.setNode("until", format.format(arequest.getUntil()));
			itemxml.setNode("after/@selected", true);
		}
		Attachments attachments = new UnmodifiableAttachments(resourceItem);

		// Exports item to blackboard
		Attachment imsOrScorm = null;
		ImsAttachment imsPackage = attachments.getIms();
		if( imsPackage != null && imsPackage.isScorm() )
		{
			imsOrScorm = imsPackage;
		}
		else
		{
			List<CustomAttachment> customList = attachments.getCustomList("scorm");
			if( customList.size() > 0 )
			{
				imsOrScorm = customList.get(0);
			}
		}

		BlackboardExport blackBoardCall = createBlackBoardCall(data);

		String courseId = data.getCourseId();
		String contentId = data.getContentId();

		boolean create = true;
		if( imsOrScorm != null && resourceItem instanceof Item )
		{
			Item item = (Item) resourceItem;
			StringWriter out = new StringWriter();
			try( Reader in = new InputStreamReader(
				fileSystemService.read(itemFileService.getItemFile(item), imsOrScorm.getUrl() + "/imsmanifest.xml")) )
			{
				CharStreams.copy(in, out);
			}

			PropBagEx resultXml = blackBoardCall.invoke("manifest",
				new NameValue[]{new NameValue(data.getUsername(), "username"),
						new NameValue(data.getContentId(), "content_id"),
						new NameValue(data.getCourseId(), "course_id"),
						new NameValue(new Base64().encode(out.toString().getBytes("UTF-8")), "manifest"),});
			create = false;
			contentId = resultXml.getNode("content_id");
		}

		// If the selected object is the item
		if( resource.getType() == SelectedResource.TYPE_PATH && resource.getUrl().length() == 0 )
		{
			resource.setUrl("./");
		}
		else if( resource.getType() == SelectedResource.TYPE_ATTACHMENT )
		{
			itemxml.setNode("attachments/@selectedType",
				linkDetails.getResourceAttachment().getAttachmentType().name().toLowerCase());
		}

		final LmsLink lmsLink = linkDetails.getLmsLink();
		final ObjectMapper mapper = objectMapperService.createObjectMapper(JaxbMapperExtension.NAME);
		final ObjectNode link = mapper.convertValue(lmsLink, ObjectNode.class);

		for( IntegrationSessionExtension extension : getExtensions() )
		{
			extension.processResultForMultiple(info, session, link, resourceItem, resource);
		}

		itemxml.setNode("attachments/@selected", nodeValue(link, "url", ""));
		itemxml.setNode("attachments/@selectedTitle", nodeValue(link, "name", ""));
		String description = nodeValue(link, "description", null);
		if( !Check.isEmpty(description) )
		{
			itemxml.setNode("attachments/@selectedDescription", description);
		}
		String folder = nodeValue(link, "folder", null);
		if( folder != null )
		{
			itemxml.setNode("attachments/@targetFolder", folder);
		}

		blackBoardCall.addContent(courseId, contentId);

		return blackBoardCall.exportItems(xml, false, create);
	}

	@Nullable
	private String nodeValue(ObjectNode obj, String nodeName, @Nullable String defaultValue)
	{
		JsonNode jsonNode = obj.get(nodeName);
		if( jsonNode == null )
		{
			return defaultValue;
		}
		return jsonNode.asText();
	}

	@Override
	public String getClose(BlackBoardSessionData data)
	{
		String closeURL = null;
		if( data.canSelect() )
		{
			BlackboardExport blackBoardCall = createBlackBoardCall(data);
			try
			{
				closeURL = blackBoardCall.redirContent(data.getCourseId(), data.getContentId(), true).toString();
			}
			catch( IOException e )
			{
				LOGGER.error("Error getting preview url");
			}
		}
		return closeURL;
	}

	private BlackboardExport createBlackBoardCall(BlackBoardSessionData data)
	{
		BlackboardExport blackboardExport = new BlackboardExport();
		try
		{
			blackboardExport.setURL(new URL(data.getBbUrl()));
			blackboardExport.setData(data);
		}
		catch( MalformedURLException e )
		{
			throw new RuntimeException(e);
		}
		return blackboardExport;
	}

	@Override
	public String getCourseInfoCode(BlackBoardSessionData data)
	{
		return data.getCourseInfoCode();
	}

	@Override
	public NameValue getLocation(BlackBoardSessionData data)
	{
		return data.getLocation();
	}

	public Class<?> getIntegrationClass()
	{
		return getClass();
	}

	@Override
	protected boolean canSelect(BlackBoardSessionData data)
	{
		return data.canSelect();
	}

	public void initSession(BlackBoardSessionData data)
	{
		List<NameValue> parameters = new ArrayList<NameValue>();

		String courseId = data.getCourseId();
		String contentId = data.getContentId();
		if( courseId != null )
		{
			if( contentId != null )
			{
				parameters.add(new NameValue(contentId, "content_id"));
			}
			parameters.add(new NameValue(courseId, "course_id"));
		}
		parameters.add(new NameValue(institutionService.getInstitutionUrl().toString(), "url"));

		String entryUrl = data.getEntryUrl();
		if( entryUrl.toLowerCase().contains(".jnlp") )
		{
			try
			{
				entryUrl += (entryUrl.indexOf('?') >= 0 ? '&' : '?') + "bburl="
					+ URLEncoder.encode(data.getBbUrl(), "UTF-8") + "&bbsession="
					+ URLEncoder.encode(data.getBlackBoardSession(), "UTF-8");
			}
			catch( UnsupportedEncodingException e )
			{
				throw Throwables.propagate(e);
			}
			data.setEntryUrl(entryUrl);
		}
	}

	@Override
	protected <I extends IItem<?>> ViewableItem<I> createViewableItem(I item, SelectedResource resource)
	{
		final ViewableItem<I> vitem = viewableItemResolver.createIntegrationViewableItem(item, resource.isLatest(),
			ViewableItemType.BLACKBOARD, resource.getKey().getExtensionType());
		return vitem;
	}

	@Override
	public <I extends IItem<?>> ViewableItem<I> createViewableItem(ItemId itemId, boolean latest,
		String itemExtensionType)
	{
		final ViewableItem<I> vitem = viewableItemResolver.createIntegrationViewableItem(itemId, latest,
			ViewableItemType.BLACKBOARD, itemExtensionType);
		return vitem;
	}
}
