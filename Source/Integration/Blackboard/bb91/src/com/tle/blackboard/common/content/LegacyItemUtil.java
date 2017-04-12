package com.tle.blackboard.common.content;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import blackboard.data.content.Content;
import blackboard.data.course.Course;
import blackboard.persist.Id;
import blackboard.platform.log.LogService;
import blackboard.platform.log.LogServiceFactory;

import com.tle.blackboard.common.BbUtil;
import com.tle.blackboard.common.propbag.PropBagMin;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
// @NonNullByDefault
public class LegacyItemUtil
{
	private static final LogService LOGGER = LogServiceFactory.getInstance();

	private static final Pattern PATTERN_URL = Pattern.compile(".*&page=([^\"|^&]*).*", Pattern.DOTALL);

	// @formatter:off
	// {0} = Equella url
	// {1} = Description
	// {2} = Blackboard relative url
	// {3} = Title
	// {4} = Item url
	// {5} = Item xml
	// {6} = Attachment name
	// {7} = Icon url
	public static final String HTML_TEMPLATE = "<!--{5}--><table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"font-size:12pt\" width=\"450px\">"
			+ "<tr><td colspan=\"2\"><img src=\"{0}/images/spacer.gif\" alt=\" \" style=\"border:none; width:0px; height:4px;\"></td></tr>"
			+ "<tr><td valign=\"top\"><table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"font-size:12pt\" width=\"100%\"><tr><td class=\"bbdesc\">{1}</td></tr>"
			+ "<tr><td colspan=\"2\"><img src=\"{0}/images/spacer.gif\" alt=\" \" style=\"border:none; width:0px; height:4px;\"></td></tr>"
			+ "<tr><td><table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"font-size:12pt\" ><td ><img src=\"{0}/{7}\" alt=\"*\" style=\"border:none;\" /></td>"
			+ "<td>&nbsp;&nbsp;<a href=\"{2}ViewContent?type=default&content_id=@X@content.pk_string@X@&course_id=@X@course.pk_string@X@&page={4}\" class=\"info\">{6}</a></td></table></td></tr></table></td></tr></table>";

	// @formatter:on

	public static String getHtml(String serverUrl, String xmlString)
	{
		PropBagMin xml = new PropBagMin(xmlString);

		String attTitle = xml.getNode("attachments/@selectedTitle");
		String selectedPage = xml.getNode("attachments/@selected");
		String selectedAttachmentType = xml.getNode("attachments/@selectedType");
		String description = xml.getNode("description");
		String title = xml.getNode("name");
		String url = selectedPage;

		String fileForIcon = selectedPage;
		if( "link".equals(selectedAttachmentType) )
		{
			fileForIcon = "http://";
		}

		if( serverUrl.endsWith("/") )
		{
			serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
		}

		final String html = MessageFormat.format(HTML_TEMPLATE, serverUrl, BbUtil.ent(description),
			BbUtil.getBlockRelativePath(), BbUtil.ent(title), url, xml, BbUtil.ent(attTitle),
			ItemUtil.getIcon(fileForIcon));
		return html;
	}

	public static ItemInfo getItemInfo(Content content, Course course, Id folderId, String equellaUrl)
	{
		String body = extractXmlFromBody(content.getBody().getFormattedText());
		final PropBagMin propBag = createPropBag(body);
		return parseXml(propBag, content.getId().toExternalString(), course.getId().toExternalString(),
			folderId.toExternalString(), equellaUrl);
	}

	// Legacy items can have XML that is not always clean. This method covers
	// one method of scrubbing the XML and allows future additions as clients
	// need them.
	protected static PropBagMin createPropBag(String body)
	{
		try
		{
			// Try with the original legacy XML
			return new PropBagMin(body);
		}
		catch( final Exception e )
		{
			// That failed. Escape the '&' in the XML and try again
			try
			{
				String ampEscapedBody = fixUnescapedAmpersands(body);
				return new PropBagMin(ampEscapedBody);
			}
			catch( final Exception e2 )
			{
				// Possible future enhancement to escape entities such as &nbsp;
				// or &blahblah; - for now, fail with an error.
				LOGGER.logError("Error parsing body of item \n" + body, e2);
				throw new RuntimeException(e2);
			}
		}
	}

	// Due to EQ-2260. When &'s are in the item body xml, Equella 6.2+ building
	// block fails. Maybe not 100% effective, but should handle the vast
	// majority of cases
	protected static String fixUnescapedAmpersands(String original)
	{
		final String regex = "&([^;\\W]*([^;\\w]|$))";
		final String replacement = "&amp;$1";
		// The double replaceAll is to handle cases where there are multiple &
		// together.
		return original.replaceAll(regex, replacement).replaceAll(regex, replacement);
	}

	// Due to EQ-2261. Legacy pages are not always ready to be directly called
	// and need to be cleaned up
	public static String scrubLegacyPage(String original)
	{
		// Due to EQAS-403 / EQ-2306
		if( (original == null) || original.equals("./") )
		{
			return "";
		}

		// When requesting pages that have folder structures in the item,
		// don't scrub the path (it should be already clean)
		int filenameIndex = original.lastIndexOf("/");
		if( filenameIndex != -1 )
		{
			return original;
		}

		// There is no folder structure. Scrub the path. Note - Equella doesn't
		// seem to like the escape character for space (+) - switch to %20
		return urlEncode(original).replaceAll("\\+", "%20");
	}

	public static String extractXmlFromBody(String body)
	{
		if( body.length() == 0 || !body.startsWith("<!--") )
		{
			BbUtil.error("** Invalid body of EQUELLA BB Content **", null);
			BbUtil.error(body, null);
			throw new RuntimeException("Broken body on legacy item");
		}

		final int temp = body.indexOf("-->");
		return body.substring(4, temp);
	}

	public static String extractUrlFromBody(String body)
	{
		// This is grotesque. Maybe this should also be recorded in the usage
		// table?
		final Matcher matcher = PATTERN_URL.matcher(body);
		matcher.matches();
		return matcher.group(1);
	}

	private static ItemInfo parseXml(PropBagMin xml, String contentId, String courseId, String folderId,
		String equellaUrl)
	{
		if( xml.nodeExists("result") )
		{
			xml = xml.getSubtree("result");
		}
		if( xml.nodeExists("item") && !xml.getNodeName().equals("item") )
		{
			xml = xml.getSubtree("item");
		}

		final ItemInfo itemInfo = new ItemInfo(equellaUrl, xml.getNode("@id"), xml.getIntNode("@version", 1),
			contentId, courseId, folderId, xml.getNode("attachments/@selected"));

		itemInfo.setName(xml.getNode("name"));
		itemInfo.setDescription(xml.getNode("description"));
		itemInfo.setActivateRequestUuid(xml.getNode("requestUuid", null));
		final String selectedAttachmentType = xml.getNode("attachments/@selectedType");
		if( selectedAttachmentType.equals("link") )
		{
			itemInfo.setMimeType("equella/link");
		}
		else
		{
			itemInfo.setMimeType("application/octet-stream");
		}

		return itemInfo;
	}

	public static String urlEncode(String url)
	{
		try
		{
			return URLEncoder.encode(url, "UTF-8");
		}
		catch( final UnsupportedEncodingException e )
		{
			// Never happen
			return url;
		}
	}
}