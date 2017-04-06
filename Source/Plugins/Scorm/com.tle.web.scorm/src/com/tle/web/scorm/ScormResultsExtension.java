package com.tle.web.scorm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.ItemResolver;
import com.tle.ims.service.IMSService;
import com.tle.web.integration.IntegrationSessionExtension;
import com.tle.web.integration.SingleSignonForm;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionSession;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ScormResultsExtension implements IntegrationSessionExtension
{
	private static final Logger LOGGER = Logger.getLogger(ScormResultsExtension.class);

	@Inject
	private ItemResolver itemResolver;
	@Inject
	private IMSService imsService;

	@Override
	public void setupSession(SectionInfo info, SelectionSession session, SingleSignonForm form)
	{
		// Nah
	}

	@Override
	public void processResultForSingle(SectionInfo info, SelectionSession session, Map<String, String> params,
		String prefix, IItem<?> item, SelectedResource resource)
	{
		// not supported
	}

	@Override
	public void processResultForMultiple(SectionInfo info, SelectionSession session, ObjectNode link, IItem<?> item,
		SelectedResource resource)
	{
		String attachmentUuid = resource.getAttachmentUuid();
		if( !Check.isEmpty(attachmentUuid) )
		{
			ItemId itemId = getItemIdForResource(resource);

			IAttachment attachment = itemResolver.getAttachmentForUuid(itemId, attachmentUuid, resource.getKey()
				.getExtensionType());
			AttachmentType type = attachment.getAttachmentType();
			if( type.equals(AttachmentType.CUSTOM) )
			{
				final CustomAttachment custom = (CustomAttachment) attachment;
				if( custom.getType().equalsIgnoreCase(ScormUtils.ATTACHMENT_TYPE) )
				{
					link.put("uuid", itemId.getUuid());
					link.put("version", itemId.getVersion());
					link.put("filename", custom.getUrl());

					try (InputStream in = imsService
						.getImsManifestAsStream(new ItemFile(itemId), custom.getUrl(), true))
					{
						link.put("scorm", IOUtils.toString(in));
					}
					catch( IOException ex )
					{
						LOGGER.error("Error reading manifest file", ex);
					}
				}
			}
		}
	}

	private ItemId getItemIdForResource(SelectedResource resource)
	{
		String uuid = resource.getUuid();
		if( resource.isLatest() )
		{
			int latestVersion = itemResolver.getLiveItemVersion(uuid, resource.getKey().getExtensionType());
			return new ItemId(uuid, latestVersion);
		}
		else
		{
			return new ItemId(uuid, resource.getVersion());
		}
	}
}
