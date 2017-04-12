package com.tle.blackboard.buildingblock.data;

import static com.tle.blackboard.common.BbUtil.CONTENT_ID;
import static com.tle.blackboard.common.BbUtil.COURSE_ID;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import blackboard.base.InitializationException;
import blackboard.data.ExtendedData;
import blackboard.data.content.Content;
import blackboard.data.content.CourseDocument;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;
import blackboard.persist.content.ContentDbLoader;
import blackboard.platform.plugin.PlugInUtil;
import blackboard.platform.session.BbSession;
import blackboard.platform.session.BbSessionManagerServiceExFactory;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.tle.blackboard.buildingblock.Configuration;
import com.tle.blackboard.common.BbContext;
import com.tle.blackboard.common.BbUtil;
import com.tle.blackboard.common.PathUtils;
import com.tle.blackboard.common.content.ContentUtil;
import com.tle.blackboard.common.content.ItemInfo;
import com.tle.blackboard.common.content.ItemUtil;
import com.tle.blackboard.common.content.LegacyItemUtil;

@SuppressWarnings("nls")
public class WrappedContent implements Comparable<WrappedContent>
{
	private static final BbContext context = BbContext.instance();

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final Id id;
	private final WrappedCourse course;
	private boolean displayUntil;
	private boolean displayAfter;
	// Lazy
	private Content content;
	// Lazy
	private WrappedExtendedData extendedData;

	public WrappedContent(String courseId, String contentId)
	{
		this(new WrappedCourse(courseId), contentId);
	}

	public WrappedContent(WrappedCourse course, String contentId)
	{
		this(course, BbUtil.getContentId(contentId));
	}

	public WrappedContent(WrappedCourse course, Id contentId)
	{
		this.course = course;
		this.id = contentId;
	}

	@SuppressWarnings("null")
	public WrappedContent(WrappedCourse course, Content content)
	{
		this(course, content.getId());
		this.content = content;
	}

	@SuppressWarnings("null")
	public WrappedContent(HttpServletRequest request)
	{
		this(request.getParameter(BbUtil.COURSE_ID), request.getParameter(BbUtil.CONTENT_ID));
	}

	public Id getId()
	{
		return id;
	}

	public WrappedCourse getCourse()
	{
		return course;
	}

	public String getTitle()
	{
		return getExtendedData().getTitle();
	}

	public void setTitle(String title)
	{
		getContent().setTitle(title);
	}

	public boolean isAvailable()
	{
		return getContent().getIsAvailable();
	}

	public void setAvailable(boolean available)
	{
		getContent().setIsAvailable(available);
	}

	public void setNewWindow(boolean newWindow)
	{
		getContent().setLaunchInNewWindow(newWindow);
	}

	public boolean isNewWindow()
	{
		return getContent().getLaunchInNewWindow();
	}

	public Calendar getEndDate()
	{
		return getContent().getEndDate();
	}

	public void setEndDate(Calendar endDate)
	{
		getContent().setEndDate(endDate);
	}

	public Calendar getStartDate()
	{
		return getContent().getStartDate();
	}

	public void setStartDate(Calendar startDate)
	{
		getContent().setStartDate(startDate);
	}

	public boolean isFolder()
	{
		return getContent().getIsFolder();
	}

	public void setFolder(boolean isFolder)
	{
		getContent().setIsFolder(isFolder);
	}

	public boolean getTrackViews()
	{
		return getContent().getIsTracked();
	}

	public void setTrackViews(boolean trackViews)
	{
		getContent().setIsTracked(trackViews);
	}

	public boolean isDescribed()
	{
		return getContent().getIsDescribed();
	}

	public void setDescribed(boolean described)
	{
		getContent().setIsDescribed(described);
	}

	public boolean isLaunch()
	{
		return getContent().getLaunchInNewWindow();
	}

	public void setLaunch(boolean launch)
	{
		getContent().setLaunchInNewWindow(launch);
	}

	public boolean isDisplayAfter()
	{
		return displayAfter;
	}

	public void setDisplayAfter(boolean displayAfter)
	{
		this.displayAfter = displayAfter;
	}

	public boolean isDisplayUntil()
	{
		return displayUntil;
	}

	public void setDisplayUntil(boolean displayUntil)
	{
		this.displayUntil = displayUntil;
	}

	public int getPosition()
	{
		return getContent().getPosition();
	}

	public void setPosition(int position)
	{
		getContent().setPosition(position);
	}

	public Id getParentId()
	{
		return getContent().getParentId();
	}

	public String getUrl()
	{
		return getExtendedData().getValue(ContentUtil.FIELD_URL);
	}

	public void setUrl(String url)
	{
		getExtendedData().setValue(ContentUtil.FIELD_URL, url);
	}

	public String getUuid()
	{
		return getExtendedData().getValue(ContentUtil.FIELD_UUID);
	}

	public void setUuid(String uuid)
	{
		getExtendedData().setValue(ContentUtil.FIELD_UUID, uuid);
	}

	public int getVersion()
	{
		final String versionString = getExtendedData().getValue(ContentUtil.FIELD_VERSION);
		if( versionString == null )
		{
			return 0;
		}
		return Integer.parseInt(versionString);
	}

	public void setVersion(int version)
	{
		getExtendedData().setValue(ContentUtil.FIELD_VERSION, Integer.toString(version));
	}

	public String getActivateRequestUuid()
	{
		return getExtendedData().getValue(ContentUtil.FIELD_ACTIVATIONUUID);
	}

	public void setActivateRequestUuid(String activateRequestUuid)
	{
		getExtendedData().setValue(ContentUtil.FIELD_ACTIVATIONUUID, activateRequestUuid);
	}

	public String getDescription()
	{
		return getExtendedData().getValue(ContentUtil.FIELD_DESCRIPTION);
	}

	public void setDescription(String description)
	{
		getExtendedData().setValue(ContentUtil.FIELD_DESCRIPTION, description);
	}

	public String getAttachmentName()
	{
		return getExtendedData().getValue(ContentUtil.FIELD_ATTACHMENT_NAME);
	}

	public void setAttachmentName(String attachmentName)
	{
		getExtendedData().setValue(ContentUtil.FIELD_ATTACHMENT_NAME, attachmentName);
	}

	public String getMimeType()
	{
		return getExtendedData().getValue(ContentUtil.FIELD_MIME_TYPE);
	}

	public void setMimeType(String mimeType)
	{
		getExtendedData().setValue(ContentUtil.FIELD_MIME_TYPE, mimeType);
	}

	private synchronized WrappedExtendedData getExtendedData()
	{
		if( extendedData == null )
		{
			final Content content = getContent();
			ExtendedData ext = content.getExtendedData();
			boolean wasNull = false;
			if( ext == null )
			{
				wasNull = true;
				ext = new ExtendedData();
				content.setExtendedData(ext);
			}
			extendedData = new WrappedExtendedData(ext, wasNull);
		}
		return extendedData;
	}

	/**
	 * The HTML to display in the content's summary
	 * 
	 * @param request
	 * @param bbEvaluateURLs Use for modify.jsp and modify_proc.jsp.  modify.jsp *used* to replace the BB placeholders. 
	 * Don't know what's happened there.
	 */
	public String getHtml(HttpServletRequest request, boolean bbEvaluateURLs)
	{
		String html = ItemUtil.getHtml(Configuration.instance().getEquellaUrl(), getUrl(), getAttachmentName(),
			getMimeType(), getTitle(), getDescription(), isNewWindow());
		if( bbEvaluateURLs )
		{
			if( request != null && bbEvaluateURLs )
			{
				final BbSession bbSession = BbSessionManagerServiceExFactory.getInstance().getSession(request);
				html = blackboardEscape(request, bbSession, html);
			}
		}
		return html;
	}

	public void startSelectionSession(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		//		if( wrapSelectionSession )
		//		{
		//			ContentServlet.forwardToContentWrapper(request, response, "AddContentBody");
		//		}
		//		else
		//		{
		final StringBuilder resourceHref = new StringBuilder(PathUtils.urlPath(BbUtil.getBlockRelativePath(),
			"AddContentBody"));
		// Foward our params
		final String queryString = request.getQueryString();
		if( !Strings.isNullOrEmpty(queryString) )
		{
			resourceHref.append("?").append(queryString);
			// To make gradebook work. Pass on the content id to the inner iframe URL
			if( !queryString.contains(CONTENT_ID) )
			{
				resourceHref.append("&" + CONTENT_ID + "=").append(
					URLEncoder.encode(getId().toExternalString(), "UTF-8"));
			}
		}
		response.sendRedirect(resourceHref.toString());
		//}
	}

	/**
	 * Note: does not use Blackboard's sepecial placeholders for courseId,
	 * contentId
	 * 
	 * @return
	 */
	public String getParameterString()
	{
		return COURSE_ID + "=" + course.getId().toExternalString() + "&" + CONTENT_ID + "=" + id.toExternalString();
	}

	@Override
	public int compareTo(WrappedContent content)
	{
		int diff = getPosition() - content.getPosition();
		if( diff == 0 )
		{
			diff = -1;
		}
		return diff;
	}

	public String getBaseParameters(HttpServletRequest request)
	{
		return blackboardEscape(request, null, BbUtil.getParamBase());
	}

	private String blackboardEscape(HttpServletRequest request, BbSession bbSession, String text)
	{
		if( request != null )
		{
			if( bbSession == null )
			{
				bbSession = BbSessionManagerServiceExFactory.getInstance().getSession(request);
			}

			try
			{
				return bbSession.encodeTemplateUrl(request, text);
			}
			catch( final InitializationException e )
			{
				// don't care, not as if we can do anything about it
			}
			catch( final PersistenceException e )
			{
				// ditto
			}
		}
		return text;
	}

	public String getReferrer(boolean modify) throws Exception
	{
		if( modify )
		{
			return PlugInUtil.getEditableContentReturnURL(content.getId(), course.getCourse().getId());
		}
		else
		{
			return PlugInUtil.getDisplayContentReturnURL(content.getId(), course.getCourse().getId());
		}
	}

	private ContentDbLoader getLoader() throws Exception
	{
		return (ContentDbLoader) context.getPersistenceManager().getLoader(ContentDbLoader.TYPE);
	}

	public void load(HttpServletRequest request) throws Exception
	{
		if( content == null )
		{
			content = getLoader().loadById(BbUtil.getContentId(request.getParameter("content_id")));
		}

		final Calendar endDate = getEndDate();
		displayUntil = endDate != null;
		if( !displayUntil )
		{
			setEndDate(Calendar.getInstance());
		}

		final Calendar startDate = getStartDate();
		displayAfter = startDate != null;
		if( !displayAfter )
		{
			setStartDate(Calendar.getInstance());
		}
	}

	public void persist(HttpServletRequest request) throws Exception
	{
		BbUtil.trace("persist(request)");
		final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
		try
		{
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			boolean isNew = false;
			if( content == null )
			{
				BbUtil.trace("content shouldn't be null here...");
				isNew = true;
				content = new CourseDocument();
				content.setPosition(-1);
				content.setContentHandler(BbUtil.CONTENT_HANDLER);
			}
			if( !displayAfter )
			{
				BbUtil.trace("set start date to null");
				content.setStartDate(null);
			}
			if( !displayUntil )
			{
				BbUtil.trace("set end date to null");
				content.setEndDate(null);
			}

			BbUtil.trace("Getting config");
			final Configuration configuration = Configuration.instance();
			final String equellaUrl = configuration.getEquellaUrl();
			BbUtil.trace("EQUELLA URL is " + equellaUrl);
			BbUtil.trace("Calling persistContent");
			ContentUtil.instance().persistContent(content, getCourse().getCourse(), equellaUrl, isNew);
		}
		catch( Exception e )
		{
			BbUtil.error("Error persisting content", e);
			throw Throwables.propagate(e);
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(oldLoader);
		}
	}

	public void remove(HttpServletRequest request) throws Exception
	{
		BbUtil.debug("remove(request)");
		final WrappedUser user = WrappedUser.getUser(request);
		try
		{
			BbUtil.trace("Getting config");
			final Configuration config = Configuration.instance();
			final String equellaUrl = config.getEquellaUrl();
			BbUtil.trace("EQUELLA URL is " + equellaUrl);
			BbUtil.trace("Calling removeContent");
			ContentUtil.instance().removeContent(getContent(), getCourse().getCourse(), equellaUrl, user.getToken());
		}
		finally
		{
			user.clearContext();
		}
	}

	public void modify(HttpServletRequest request) throws Exception
	{
		BbUtil.trace("modify(request)");
		try
		{
			final String name = request.getParameter("name");
			final String description = request.getParameter("description");
			final String available = request.getParameter("available");
			final String views = request.getParameter("views");
			final String described = request.getParameter("described");
			final String startDateString = request.getParameter("date_start_datetime");
			final String endDateString = request.getParameter("date_end_datetime");
			final String displayAfterString = request.getParameter("date_start_checkbox");
			final String displayUntilString = request.getParameter("date_end_checkbox");
			final String newWindowString = request.getParameter("newWindow");
			BbUtil.trace("name=" + name);
			BbUtil.trace("description=" + description);
			BbUtil.trace("available=" + available);
			BbUtil.trace("views=" + views);
			BbUtil.trace("described=" + described);
			BbUtil.trace("startDateString=" + startDateString);
			BbUtil.trace("endDateString=" + endDateString);
			BbUtil.trace("displayAfterString=" + displayAfterString);
			BbUtil.trace("displayUntilString=" + displayUntilString);
			BbUtil.trace("newWindowString=" + newWindowString);

			//Kill legacy shiite.
			killLegacyFields();

			setTitle(name);
			setDescription(description);
			setAvailable(available != null && available.equals("true"));
			setTrackViews(views != null && views.equals("true"));
			setDescribed(described != null && described.equals("true"));
			setDisplayAfter(displayAfterString != null && displayAfterString.equals("1"));
			setDisplayUntil(displayUntilString != null && displayUntilString.equals("1"));
			setNewWindow("true".equals(newWindowString));
			if( !Strings.isNullOrEmpty(startDateString) )
			{
				final Calendar startDate = Calendar.getInstance();
				startDate.setTime(DATE_FORMAT.parse(startDateString));
				setStartDate(startDate);
			}
			if( !Strings.isNullOrEmpty(endDateString) )
			{
				final Calendar endDate = Calendar.getInstance();
				endDate.setTime(DATE_FORMAT.parse(endDateString));
				setEndDate(endDate);
			}
		}
		catch( Exception e )
		{
			BbUtil.error("Error modifying content", e);
			throw Throwables.propagate(e);
		}
	}

	/**
	 * Removes the commented out XML bollocks and puts into Extended Data
	 */
	private void killLegacyFields()
	{
		final Content c = getContent();
		final String body = c.getBody().getFormattedText();
		if( body.length() == 0 )
		{
			return;
		}
		if( body.startsWith("<!--") )
		{
			// This *looks* completely ineffectual but is actually not. 
			// It will read from the legacy location and put into the proper location.
			setTitle(getTitle());
			setDescription(getDescription());
			setUrl(getUrl());
			setUuid(getUuid());
			setVersion(getVersion());
			setAttachmentName(getAttachmentName());
			setMimeType(getMimeType());
		}
	}

	private String nullToEmpty(String value)
	{
		if( value == null )
		{
			return "";
		}
		return value;
	}

	@SuppressWarnings("null")
	public synchronized Content getContent()
	{
		if( content == null )
		{
			BbUtil.trace("content is null, loading via ID=" + getId().toExternalString());
			try
			{
				content = ContentUtil.instance().loadContent(getId());
				BbUtil.trace("content type=" + content.getClass().getCanonicalName());
			}
			catch( final Exception e )
			{
				BbUtil.error("Error getting content", e);
				throw Throwables.propagate(e);
			}
		}
		return content;
	}

	// Legacy crapness
	@SuppressWarnings("unused")
	protected String getLaunchUrl(String itemUrl, String page)
	{
		if( page != null )
		{
			try
			{
				new URL(page);
				// is a URL, don't modify it
				return page;
			}
			catch( MalformedURLException mal )
			{
				return PathUtils.urlPath(itemUrl, page);
			}
		}
		return itemUrl;
	}

	private class WrappedExtendedData
	{
		private final ExtendedData data;

		private final ItemInfo itemInfo;

		public WrappedExtendedData(ExtendedData data, boolean wasNull)
		{
			this.data = data;
			final Content cont = getContent();
			if( wasNull || ContentUtil.instance().isLegacy(cont) )
			{
				final Configuration config = Configuration.instance();
				final String equellaUrl = config.getEquellaUrl();
				itemInfo = LegacyItemUtil.getItemInfo(cont, getCourse().getCourse(), cont.getParentId(), equellaUrl);
			}
			else
			{
				itemInfo = null;
			}
		}

		public String getTitle()
		{
			String title = getContent().getTitle();
			if( title == null )
			{
				title = itemInfo.getName();
			}
			if( title != null )
			{
				return title;
			}
			return "";
		}

		public String getValue(String key)
		{
			String value = data.getValue(key);
			if( value == null && itemInfo != null )
			{
				if( ContentUtil.FIELD_DESCRIPTION.equals(key) )
				{
					String description = itemInfo.getDescription();
					if( description != null )
					{
						return description;
					}
					return "";
				}
				if( ContentUtil.FIELD_UUID.equals(key) )
				{
					return itemInfo.getItemKey().getUuid();
				}
				if( ContentUtil.FIELD_VERSION.equals(key) )
				{
					return Integer.toString(itemInfo.getItemKey().getVersion());
				}
				if( ContentUtil.FIELD_ACTIVATIONUUID.equals(key) )
				{
					return itemInfo.getActivateRequestUuid();
				}
				if( ContentUtil.FIELD_ATTACHMENT_NAME.equals(key) )
				{
					return itemInfo.getAttachmentName();
				}
				if( ContentUtil.FIELD_URL.equals(key) )
				{
					return getLaunchUrl(
						PathUtils.urlPath("integ/gen", itemInfo.getItemKey().getUuid(),
							Integer.toString(itemInfo.getItemKey().getVersion())), itemInfo.getItemKey().getPage());
				}
				if( ContentUtil.FIELD_MIME_TYPE.equals(key) )
				{
					return itemInfo.getMimeType();
				}
			}
			return value;
		}

		public void setValue(String key, String value)
		{
			data.setValue(key, nullToEmpty(value));
		}
	}
}