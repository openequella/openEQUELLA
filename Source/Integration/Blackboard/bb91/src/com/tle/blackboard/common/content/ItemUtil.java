package com.tle.blackboard.common.content;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import blackboard.data.ExtendedData;
import blackboard.data.content.Content;
import blackboard.data.course.Course;
import blackboard.persist.Id;

import com.google.common.base.Strings;
import com.tle.blackboard.common.BbUtil;
import com.tle.blackboard.common.PathUtils;
import com.tle.blackboard.common.propbag.PropBagMin;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
// @NonNullByDefault
public class ItemUtil
{
	private static final String DEFAULT_ICON_ID = "attachment";
	private static final String LINK_ICON_ID = ".html";
	private static final String DEFAULT_ICON_ICON = "icons/attachment.gif";

	private static final Logger LOGGER = Logger.getLogger(ItemUtil.class);
	private static final Map<String, String> icons = getIconMap();

	// @formatter:off
	// {0} = Equella url (without trailing slash)
	// {1} = Icon url (without preceding slash)
	// {2} = File type
	// {3} = ViewContent url (without trailing slash)
	// {4} = Attachment name
	// {5} = Link target (for popups)
	private static final String HTML_TEMPLATE = 
		"<div class=\"equella-link contextItemDetailsHeaders\"><a {5} href=\"{3}?content_id=@X@content.pk_string@X@&course_id=@X@course.pk_string@X@\">"
		+     "<img alt=\"{2}\" src=\"{0}/{1}\" style=\"margin-right:10px\">{4}"
		+ "</a></div>";
	// @formatter:on

	public static String getHtml(String serverUrl, String itemUrl, String attachmentName, String mimeType,
		String itemTitle, String itemDescription, boolean newWindow)
	{
		String fileForIcon = itemUrl;
		if( "equella/link".equals(mimeType) )
		{
			fileForIcon = "http://";
		}

		if( serverUrl.endsWith("/") )
		{
			serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
		}

		final String linkTitle = BbUtil.ent(Strings.isNullOrEmpty(attachmentName) ? itemTitle : attachmentName);
		final String alt = (mimeType == null ? linkTitle : mimeType);
		final String html = MessageFormat.format(ItemUtil.HTML_TEMPLATE, serverUrl, getIcon(fileForIcon), alt,
			PathUtils.urlPath(BbUtil.getBlockRelativePath(), "ViewContent"), linkTitle, (newWindow
				? "target=\"_blank\"" : ""));
		if( !Strings.isNullOrEmpty(itemDescription) )
		{
			return html + "<div class=\"equella-description\" style=\"margin-top:10px\"><p>"
				+ BbUtil.ent(itemDescription) + "</p></div>";
		}
		return html;
	}

	public static ItemInfo getItemInfo(Content content, Course course, Id folderId, String equellaUrl)
	{
		if( ContentUtil.instance().isLegacy(content) )
		{
			return LegacyItemUtil.getItemInfo(content, course, folderId, equellaUrl);
		}
		final ExtendedData extendedData = content.getExtendedData();
		final ItemInfo info = new ItemInfo(equellaUrl, extendedData.getValue(ContentUtil.FIELD_UUID),
			Integer.parseInt(extendedData.getValue(ContentUtil.FIELD_VERSION)), content.getId().toExternalString(),
			course.getId().toExternalString(), folderId.toExternalString(),
			extendedData.getValue(ContentUtil.FIELD_URL));
		info.setAvailable(content.getIsAvailable());
		info.setDescription(extendedData.getValue(ContentUtil.FIELD_DESCRIPTION));
		//Note: we are using ModifiedDate for CreatedDate now.  CreatedDate was deprecated and could go away any day now.
		final Calendar modifiedDate = content.getModifiedDate();
		if( modifiedDate != null )
		{
			info.setCreatedDate(modifiedDate.getTime());
			info.setModifiedDate(modifiedDate.getTime());
		}
		info.setName(content.getTitle());
		info.setActivateRequestUuid(extendedData.getValue(ContentUtil.FIELD_ACTIVATIONUUID));
		info.setAttachmentName(extendedData.getValue(ContentUtil.FIELD_ATTACHMENT_NAME));
		info.setMimeType(extendedData.getValue(ContentUtil.FIELD_MIME_TYPE));
		return info;
	}

	private static Map<String, String> getIconMap()
	{
		HashMap<String, String> iconMap = new HashMap<String, String>();
		try
		{
			PropBagMin xml = new PropBagMin(ItemUtil.class.getResourceAsStream("/icons.xml"));
			Iterator<PropBagMin> i = xml.iterator("types/type");
			HashMap<String, String> tempMap = new HashMap<String, String>();
			while( i.hasNext() )
			{
				PropBagMin temp = i.next();
				String id = temp.getNode("@id");
				String icon = temp.getNode("@icon");
				tempMap.put(id, icon);
			}

			i = xml.iterator("extensions/extension");
			while( i.hasNext() )
			{
				PropBagMin temp = i.next();
				String id = temp.getNode("@id");
				String type = temp.getNode("@type");
				type = tempMap.get(type);
				if( type == null )
				{
					type = DEFAULT_ICON_ICON;
				}
				iconMap.put(id, type);
			}
		}
		catch( Exception e )
		{
			LOGGER.error("Icons xml could not be found", e);
			iconMap.put(DEFAULT_ICON_ID, DEFAULT_ICON_ICON);
		}

		return iconMap;
	}

	public static String convertToRelativeUrl(String equellaUrl, String url)
	{
		// we need to de-institutionalise the url so we have a relative path
		if( url.startsWith(equellaUrl) )
		{
			return url.substring(equellaUrl.length());
		}
		return url;
	}

	public static String getIcon(String file)
	{
		if( file.startsWith("http://") )
		{
			return icons.get(LINK_ICON_ID);
		}

		if( file.endsWith(".jsp") )
		{
			final String icon = icons.get(file.toLowerCase());
			if( icon != null )
			{
				return icon;
			}
		}

		String icon = file;

		int dot = file.indexOf('.');
		if( dot < 0 )
		{
			icon = DEFAULT_ICON_ID;
		}
		else
		{
			icon = file.substring(dot);
		}

		icon = icons.get(icon);
		if( icon == null )
		{
			icon = icons.get(DEFAULT_ICON_ID);
		}

		if( icon == null )
		{
			icon = DEFAULT_ICON_ICON;
		}

		return icon;
	}
}
