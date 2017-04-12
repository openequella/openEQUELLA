package com.tle.blackboard.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import blackboard.base.BbList;
import blackboard.data.content.Content;
import blackboard.data.course.Course;
import blackboard.data.navigation.CourseToc;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.DataType;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.content.ContentDbLoader;
import blackboard.persist.course.CourseDbLoader;
import blackboard.persist.navigation.CourseTocDbLoader;
import blackboard.platform.config.ConfigurationServiceFactory;
import blackboard.platform.log.LogService;
import blackboard.platform.log.LogServiceFactory;
import blackboard.platform.plugin.PlugInUtil;
import blackboard.platform.ws.AxisHelpers;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
// @NonNullByDefault
public class BbUtil
{
	private static final LogService LOGGER = LogServiceFactory.getInstance();
	static
	{
		System.out.println("BbUtil.LOGGER is " + LOGGER.getLogFileName());
	}

	public static final String HANDLE = "tle";
	public static final String VENDOR = "dych";
	public static final String CONTENT_HANDLER = "resource/tle-resource";
	public static final String CONTEXT_TAG = "@X@";
	public static final String CONTENT_ID = "content_id";
	public static final String CONTENT_ID_PLACEHOLDER = CONTEXT_TAG + "content.pk_string" + CONTEXT_TAG;
	public static final String COURSE_ID = "course_id";
	public static final String COURSE_ID_PLACEHOLDER = CONTEXT_TAG + "course.pk_string" + CONTEXT_TAG;

	private static int majorVersionNumber;
	private static int minorVersionNumber;
	private static int revisionNumber;
	private static String bbVersionString;

	private static final boolean trace = false;
	private static final boolean sqlTrace = false;

	static
	{
		bbVersionString = ConfigurationServiceFactory.getInstance().getBbProperty("bbconfig.version.number");
		final StringTokenizer st = new StringTokenizer(bbVersionString, ".");
		if( st.hasMoreTokens() )
		{
			majorVersionNumber = Integer.parseInt(st.nextToken());
			try
			{
				minorVersionNumber = Integer.parseInt(st.nextToken());
			}
			catch( Exception e )
			{
				// so be it
				minorVersionNumber = -1;
			}

			try
			{
				revisionNumber = Integer.parseInt(st.nextToken());
			}
			catch( Exception e )
			{
				// so be it
				revisionNumber = -1;
			}
		}
	}

	public static String getBbVersionString()
	{
		return bbVersionString;
	}

	/**
	 * Called from config.jsp
	 * 
	 * @return
	 */
	public static int getMajorVersionNumber()
	{
		return majorVersionNumber;
	}

	/**
	 * Called from config.jsp
	 * 
	 * @return
	 */
	public static int getMinorVersionNumber()
	{
		return minorVersionNumber;
	}

	public static int getRevisionNumber()
	{
		return revisionNumber;
	}

	private BbUtil()
	{
		throw new Error();
	}

	public static String getParamBase()
	{
		return new StringBuilder(CONTENT_ID).append("=").append(CONTENT_ID_PLACEHOLDER).append("&").append(COURSE_ID)
			.append("=").append(COURSE_ID_PLACEHOLDER).toString();
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

	public static String getBlockRelativePath()
	{
		try
		{
			return PlugInUtil.getUriStem(VENDOR, HANDLE);
		}
		catch( final Exception t )
		{
			error("Error getting relative path", t);
			throw Throwables.propagate(t);
		}
	}

	public static Id getCourseId(String courseId)
	{
		return getId(courseId, blackboard.data.course.Course.DATA_TYPE);
	}

	public static Id getContentId(String contentId)
	{
		return getId(contentId, blackboard.data.content.Content.DATA_TYPE);
	}

	public static Id getFolderId(String folderId)
	{
		return getId(folderId, blackboard.data.content.ContentFolder.DATA_TYPE);
	}

	public static Id getUserId(String userId)
	{
		return getId(userId, blackboard.data.user.User.DATA_TYPE);
	}

	public static Id getId(String id, DataType type)
	{
		try
		{
			return BbContext.instance().getPersistenceManager().generateId(type, id);
		}
		catch( PersistenceException e )
		{
			AxisHelpers.throwWSException("EQ002", e.getMessage());
			// Should not reach here
			throw Throwables.propagate(e);
		}
	}

	/* @Nullable */
	public static Content loadContent(ContentDbLoader contentDbLoader, String id) throws PersistenceException
	{
		return loadContent(contentDbLoader, BbUtil.getContentId(id));
	}

	/* @Nullable */
	public static Content loadContent(ContentDbLoader contentDbLoader, Id id) throws PersistenceException
	{
		try
		{
			return contentDbLoader.loadById(id);
		}
		// KNF..uh huh uh huh uh huh. KNF's gunna rock ya
		// http://www.youtube.com/watch?v=LXEOESuiYcA
		catch( KeyNotFoundException knf )
		{
			return null;
		}
	}

	/* @Nullable */
	public static Course loadCourse(CourseDbLoader courseDbLoader, String id) throws PersistenceException
	{
		return loadCourse(courseDbLoader, BbUtil.getCourseId(id));
	}

	/* @Nullable */
	public static Course loadCourse(CourseDbLoader courseDbLoader, Id id) throws PersistenceException
	{
		try
		{
			return courseDbLoader.loadById(id);
		}
		// KNF..uh huh uh huh uh huh. KNF's gunna rock ya
		// http://www.youtube.com/watch?v=LXEOESuiYcA
		catch( KeyNotFoundException knf )
		{
			return null;
		}
	}

	public static Iterable<Content> getBbFoldersForCourse(Id courseId) throws PersistenceException
	{
		final BbPersistenceManager pm = BbContext.instance().getPersistenceManager();
		final CourseTocDbLoader courseTocDbLoader = (CourseTocDbLoader) pm.getLoader(CourseTocDbLoader.TYPE);
		final ContentDbLoader contentDbLoader = (ContentDbLoader) pm.getLoader(ContentDbLoader.TYPE);

		// This is the Information / Contents etc list displayed on the
		// left when viewing a course.
		// But surely there is only one per course?
		final BbList<CourseToc> bbcourseTocs = courseTocDbLoader.loadByCourseId(courseId);
		final List<Content> folders = new ArrayList<Content>();

		for( CourseToc bbcourseToc : bbcourseTocs )
		{
			// list the content
			final Id contentId = bbcourseToc.getContentId();
			if( contentId.isSet() )
			{
				final Content courseContent = contentDbLoader.loadById(contentId);
				if( courseContent.getIsFolder() )
				{
					folders.add(courseContent);
				}
			}
		}
		return folders;
	}

	/**
	 * If you need subfolders only you need to check content.isFolder for each
	 * returned content.
	 * 
	 * @param pm
	 * @param folderId
	 * @return
	 * @throws PersistenceException
	 */
	public static Iterable<Content> getBbContentForFolder(Id folderId) throws PersistenceException
	{
		final ContentDbLoader contentDbLoader = (ContentDbLoader) BbContext.instance().getPersistenceManager()
			.getLoader(ContentDbLoader.TYPE);
		return contentDbLoader.loadChildren(folderId);
	}

	public static void sqlTrace(/* @Nullable */Object text)
	{
		// dev mode only
		if( sqlTrace )
		{
			LOGGER.logDebug(text == null ? "" : text.toString());
			System.out.println(text);
		}
	}

	public static void trace(/* @Nullable */Object text)
	{
		// dev mode only
		if( trace )
		{
			LOGGER.logDebug(text == null ? "" : text.toString());
			System.out.println(text);
		}
	}

	public static void debug(String text)
	{
		LOGGER.logDebug(text);
	}

	public static void error(String text, /* @Nullable */Throwable t)
	{
		if( t != null )
		{
			LOGGER.logError(text, t);
			System.out.println(text);
			t.printStackTrace(System.out);
		}
		else
		{
			LOGGER.logError(text);
			System.out.println(text);
		}
	}

	public static String ent(String szStr)
	{
		if( Strings.isNullOrEmpty(szStr) )
		{
			return "";
		}

		StringBuilder szOut = new StringBuilder();
		final char[] chars = szStr.toCharArray();
		for( final char ch : chars )
		{
			switch( ch )
			{
				case '<':
					szOut.append("&lt;");
					break;

				case '>':
					szOut.append("&gt;");
					break;

				case '&':
					szOut.append("&amp;");
					break;

				case '"':
					szOut.append("&quot;");
					break;

				default:
					// http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char
					// regular displayable ASCII:
					if( ch == 0xA || ch == 0xD || ch == 0x9 || (ch >= 0x20 && ch <= 0x007F) )
					{
						szOut.append(ch);
					}
					else if( (ch > 0x007F && ch <= 0xD7FF) || (ch >= 0xE000 && ch <= 0xFFFD)
						|| (ch >= 0x10000 && ch <= 0x10FFFF) )
					{
						szOut.append("&#x");
						final String hexed = Integer.toHexString(ch);
						// wooo, unrolled loops
						switch( 4 - hexed.length() )
						{
							case 3:
							case 2:
							case 1:
								szOut.append('0');
								break;
							default:
								break;
						}
						szOut.append(hexed);
						szOut.append(';');
					}
					// else we discard the character entirely.
					// It CANNOT be placed in XML
					break;
			}
		}

		return szOut.toString();
	}
}
