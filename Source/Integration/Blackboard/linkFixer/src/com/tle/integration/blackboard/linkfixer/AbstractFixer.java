package com.tle.integration.blackboard.linkfixer;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.dytech.devlib.PropBagEx;

/**
 * @author aholland
 */
public abstract class AbstractFixer
{
	protected static final String CONTEXT_TAG = "@X@"; //$NON-NLS-1$

	protected static final String CONTENT_ID = "content_id"; //$NON-NLS-1$
	protected static final String COURSE_ID = "course_id"; //$NON-NLS-1$
	protected static final String BB_ID_REGEX = "((_[0-9]+_[0-9]+)|(null))";

	protected static final Pattern CONTENT_ID_REGEX = Pattern
		.compile(CONTENT_ID + "=" + BB_ID_REGEX, Pattern.MULTILINE);
	protected static final Pattern COURSE_ID_REGEX = Pattern.compile(COURSE_ID + "=" + BB_ID_REGEX, Pattern.MULTILINE);
	protected static final Pattern XML_REGEX = Pattern.compile("^<!\\-\\-(.*)\\-\\->.*");

	protected Pattern EQUELLA_URL_REGEX;
	protected static final String UUID_REGEX = "[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}";

	protected static final String EQUELLA_BLOCK_VENDOR = "dych";
	protected static final String EQUELLA_BLOCK_HANDLE = "tle";

	protected final String contentParam;
	protected final String courseParam;

	protected String equellaUrl = "";
	protected final StringBuffer log; // yes, needs to be StringBuffer

	protected AbstractFixer()
	{
		log = new StringBuffer();
		contentParam = getContentParam();
		courseParam = getCourseParam();
	}

	public abstract void submit(HttpServletRequest request) throws Exception;

	protected abstract int getBlackboardVersion();

	protected abstract String getEquellaUrl();

	protected abstract String getRelativePath();

	protected Matcher getEquellaUrlMatcher(String text)
	{
		if( EQUELLA_URL_REGEX == null )
		{
			EQUELLA_URL_REGEX = Pattern.compile(getEquellaUrl().replaceAll("\\.", "\\\\.").replaceAll("\\+", "\\\\+")
				+ "integ/bb/" + UUID_REGEX + "/[0-9]+/([^\\\"]*)", Pattern.MULTILINE);
		}
		return EQUELLA_URL_REGEX.matcher(text);
	}

	protected String fixText(String originalText, FixTextFeedback feedback)
	{
		final Matcher contentIdMatcher = CONTENT_ID_REGEX.matcher(originalText);
		String newText = contentIdMatcher.replaceAll(contentParam);

		final Matcher courseIdMatcher = COURSE_ID_REGEX.matcher(newText);
		newText = courseIdMatcher.replaceAll(courseParam);

		if( !newText.equals(originalText) )
		{
			feedback.fixedHardCodedIds = true;
		}

		final Matcher equellaMatcher = getEquellaUrlMatcher(newText);
		if( equellaMatcher.find() )
		{
			final String page = equellaMatcher.group(equellaMatcher.groupCount());

			// Luckily, the original XML used to add each BB item is contained
			// in a HTML comment
			// Nice!
			String scorm = null;
			final Matcher xmlMatcher = XML_REGEX.matcher(newText);
			if( xmlMatcher.find() )
			{
				final String xml = xmlMatcher.group(1);

				PropBagEx pxml = new PropBagEx(xml);
				for( PropBagEx attachment : pxml.iterateAll("attachments/attachment") )
				{
					if( attachment.getNode("file").equals(page) )
					{
						scorm = attachment.getNode("@scorm");
						break;
					}
				}
			}

			newText = equellaMatcher.replaceAll(getAttachmentUrl(scorm, page));
			feedback.fixedNonTokenedUrls = true;
		}

		return newText;
	}

	/**
	 * Grrrr, bb6.3 has different template names than bb7
	 * 
	 * @return
	 */
	private String getContentParam()
	{
		return new StringBuilder(CONTENT_ID).append("=").append(CONTEXT_TAG)
			.append(getBlackboardVersion() < 7 ? "content.pk_string" : "content.id").append(CONTEXT_TAG).toString();
	}

	/**
	 * Grrrr, bb6.3 has different template names than bb7
	 * 
	 * @return
	 */
	private String getCourseParam()
	{
		return new StringBuilder(COURSE_ID).append("=").append(CONTEXT_TAG)
			.append(getBlackboardVersion() < 7 ? "course.course_id" : "course.id").append(CONTEXT_TAG).toString();
	}

	public String getLog()
	{
		return log.toString();
	}

	protected void logMessage(int lvl, String msg)
	{
		log.append(loggerPadding(lvl) + msg + "\n");
	}

	private String loggerPadding(int level)
	{
		String pad = "";
		for( int lvl = 0; lvl < level; lvl++ )
		{
			pad += "  ";
		}
		return pad;
	}

	private String getAttachmentUrl(String scorm, String attachment)
	{
		if( !(attachment.startsWith("http://") || attachment.startsWith("https://")) )
		{
			String type = "default";
			if( scorm != null && scorm.trim().length() > 0 )
			{
				type = "scorm" + scorm;
			}

			return getViewContentUrl(encode(attachment), type);
		}
		return attachment;
	}

	private String getViewContentUrl(String page, String type)
	{
		String href = new StringBuilder(getRelativePath()).append("ViewContent?type=" + type + "&")
			.append(contentParam).append("&").append(courseParam).append("&page=").append(page).toString();
		return href;
	}

	protected String encode(String url)
	{
		try
		{
			return URLEncoder.encode(url, "UTF-8"); //$NON-NLS-1$
		}
		catch( final Exception e )
		{
			// Never happen
			return null;
		}
	}

	protected class FixTextFeedback
	{
		public boolean fixedHardCodedIds;
		public boolean fixedNonTokenedUrls;
	}
}
