package com.tle.blackboard.common.content;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import blackboard.base.FormattedText;
import blackboard.data.ExtendedData;
import blackboard.data.blti.BasicLTIPlacement;
import blackboard.data.content.Content;
import blackboard.data.content.CourseDocument;
import blackboard.data.course.Course;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;
import blackboard.persist.content.ContentDbLoader;
import blackboard.persist.content.ContentDbPersister;
import blackboard.platform.gradebook2.GradableItem;
import blackboard.platform.gradebook2.GradableItemManager;
import blackboard.platform.gradebook2.GradebookManagerFactory;
import blackboard.platform.security.authentication.BbSecurityException;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.tle.blackboard.common.BbContext;
import com.tle.blackboard.common.BbUtil;
import com.tle.blackboard.common.PathUtils;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class ContentUtil
{
	private static ContentUtil instance;
	private static final Object instanceLock = new Object();

	// TODO: privatise
	public static final String FIELD_DESCRIPTION = "description";
	public static final String FIELD_UUID = "uuid";
	public static final String FIELD_VERSION = "version";
	/**
	 * This is a *relative* URL. I.e. it doesn't include the institution
	 * portion.
	 */
	public static final String FIELD_URL = "url";
	public static final String FIELD_ATTACHMENT_NAME = "attachmentName";
	public static final String FIELD_ATTACHMENT_UUID = "attachmentUuid";
	public static final String FIELD_MIME_TYPE = "mimeType";
	public static final String FIELD_ACTIVATIONUUID = "activationUuid";

	private static final List<String> GRADABLE_MIMES = Arrays.asList("equella/attachment-lti", "equella/qtitest",
		"equella/scorm-package");

	private ContentDbLoader contentLoader;
	private ContentDbPersister contentPersister;

	public static ContentUtil instance()
	{
		if( instance == null )
		{
			synchronized( instanceLock )
			{
				if( instance == null )
				{
					instance = new ContentUtil();
				}
			}
		}
		return instance;
	}

	private ContentUtil()
	{
	}

	/**
	 * @param course
	 * @param folderId
	 * @param uuid
	 * @param version
	 * @param url Relative URL
	 * @param title
	 * @param description
	 * @param attachmentUuid
	 * @param attachmentName
	 * @param mimeType
	 * @param equellaUrl
	 * @return
	 */
	public Content addContent(Course course, Id folderId, String uuid, int version, String url, String title,
		String description, String attachmentUuid, String attachmentName, String mimeType, String equellaUrl,
		boolean newWindow)
	{
		try
		{
			final Content content = new CourseDocument();
			content.setIsFolder(false);
			content.setTitle(title);
			content.setCourseId(course.getId());
			content.setParentId(folderId);
			content.setIsAvailable(true);
			content.setLaunchInNewWindow(true);
			content.setContentHandler(BbUtil.CONTENT_HANDLER);
			content.setPosition(-1);
			content.setIsSequential(false);
			content.setLaunchInNewWindow(newWindow);

			ExtendedData extendedData = content.getExtendedData();
			if( extendedData == null )
			{
				extendedData = new ExtendedData();
				content.setExtendedData(extendedData);
			}
			extendedData.setValue(FIELD_UUID, uuid);
			extendedData.setValue(FIELD_VERSION, Integer.toString(version));
			extendedData.setValue(FIELD_URL, url);
			if( description != null )
			{
				extendedData.setValue(FIELD_DESCRIPTION, description);
			}
			if( attachmentUuid != null )
			{
				extendedData.setValue(FIELD_ATTACHMENT_UUID, attachmentUuid);
			}
			if( attachmentName != null )
			{
				extendedData.setValue(FIELD_ATTACHMENT_NAME, attachmentName);
			}
			if( mimeType != null )
			{
				extendedData.setValue(FIELD_MIME_TYPE, mimeType);
			}

			return persistContent(content, course, equellaUrl, true);
		}
		catch( Exception e )
		{
			BbUtil.error("Error saving new content", e);
			throw Throwables.propagate(e);
		}
	}

	public Content persistContent(Content content, Course course, String equellaUrl, boolean isNew)
	{
		try
		{
			ItemInfo itemInfo = ItemUtil.getItemInfo(content, course, content.getParentId(), equellaUrl);
			final ItemKey itemKey = itemInfo.getItemKey();

			final FormattedText text = new FormattedText(ItemUtil.getHtml(equellaUrl, itemKey.getPage(),
				itemInfo.getAttachmentName(), itemInfo.getMimeType(), content.getTitle(), itemInfo.getDescription(),
				content.getLaunchInNewWindow()), FormattedText.Type.HTML);
			content.setBody(text);

			BbUtil.trace("persisting content for url " + itemKey.getPage());
			getContentPersister().persist(content);
			BbUtil.trace("content success!");

			// need to do this again so the contentId doesn't get munged up
			itemInfo = ItemUtil.getItemInfo(content, course, content.getParentId(), equellaUrl);
			register(content, course, itemInfo, equellaUrl, isNew);
			ensureGradableItem(content, isNew, true);

			return content;
		}
		catch( Exception e )
		{
			BbUtil.error("Error persisting content", e);
			throw Throwables.propagate(e);
		}
	}

	public Content loadContent(Id contentId)
	{
		try
		{
			return getContentLoader().loadById(contentId);
		}
		catch( Exception e )
		{
			BbUtil.error("Error loading content", e);
			throw Throwables.propagate(e);
		}
	}

	public Content loadContentForViewing(Id contentId)
	{
		try
		{
			final Content content = getContentLoader().loadById(contentId);
			RegistrationUtil.updateDateAccessed(contentId.toExternalString(), new Date());
			ensureGradableItem(content, false, false);
			return content;
		}
		catch( Exception e )
		{
			BbUtil.error("Error loading content for viewing", e);
			throw Throwables.propagate(e);
		}
	}

	private void ensureGradableItem(Content content, boolean isNewContent, boolean modifyExisting) throws Exception
	{
		BbUtil.trace("ensureGradableItem");

		final String mimeType = content.getExtendedData().getValue(FIELD_MIME_TYPE);
		final BasicLTIPlacement ltiPlacement = PlacementUtil.getDefaultPlacement();
		if( ltiPlacement == null || !ltiPlacement.isAllowGrading() || !GRADABLE_MIMES.contains(mimeType) )
		{
			return;
		}

		final GradableItemManager gradableItemManager = GradebookManagerFactory
			.getGradableItemManagerWithoutSecurityCheck();
		boolean foundExisting = false;
		final GradableItem gradableItem;
		if( isNewContent )
		{
			gradableItem = createGradableItem(content);
		}
		else
		{
			GradableItem gitem = gradableItemManager.getGradebookItemByContentId(content.getId());
			if( gitem == null )
			{
				gitem = createGradableItem(content);
			}
			else
			{
				BbUtil.trace("gradable item already exists");
				foundExisting = true;
			}
			gradableItem = gitem;
		}
		if( modifyExisting || !foundExisting )
		{
			gradableItem.setTitle(content.getTitle());
			gradableItem.setCourseId(content.getCourseId());
			gradableItem.setDateModified(Calendar.getInstance());
			try
			{
				gradableItemManager.persistGradebookItem(gradableItem);
			}
			catch( BbSecurityException e )
			{
				BbUtil.error("Error saving gradableItem", e);
				throw Throwables.propagate(e);
			}
			BbUtil.trace("gradable item success!");
		}
		else
		{
			BbUtil.trace("not modifying existing gradable item");
		}
	}

	@SuppressWarnings("deprecation")
	private void register(Content content, Course course, ItemInfo itemInfo, String equellaUrl, boolean isNew)
	{
		try
		{
			// Register in EQUELLA table
			BbUtil.trace("Registering EQUELLA content in database");

			final ItemKey itemKey = itemInfo.getItemKey();

			BbUtil.debug("Registering content " + itemKey + " which is " + (isNew ? "new" : "NOT new"));

			final Date createdDate = (content.getCreatedDate() == null ? new Date() : content.getCreatedDate()
				.getTime());
			final Date modifiedDate = (content.getModifiedDate() == null ? createdDate : content.getModifiedDate()
				.getTime());

			RegistrationUtil.recordItem(itemKey, isNew, true, course.getIsAvailable(), content.getTitle(),
				itemInfo.getDescription(), itemInfo.getAttachmentName(), createdDate, modifiedDate, course.getTitle());
		}
		catch( Exception t )
		{
			BbUtil.error("An error occurred while registering content", t);
		}
	}

	private GradableItem createGradableItem(Content content)
	{
		final GradableItem gradableItem = new GradableItem();
		gradableItem.setCourseContentId(content.getId());
		gradableItem.setScoreProviderHandle(BbUtil.CONTENT_HANDLER);
		gradableItem.setVisibleInBook(true);
		gradableItem.setVisibleToStudents(true);
		gradableItem.setPoints(100);
		gradableItem.setHideAttempt(false);
		gradableItem.setDateAdded(Calendar.getInstance());
		gradableItem.setScorable(true);
		return gradableItem;
	}

	public void removeContent(Content content, Course course, String equellaUrl, String token)
	{
		try
		{
			final ItemInfo itemInfo = ItemUtil.getItemInfo(content, course, content.getParentId(), equellaUrl);

			unregister(itemInfo.getItemKey());
			deactivate(itemInfo, equellaUrl, token);
			removeGradableItem(content);

			getContentPersister().deleteById(content.getId());
		}
		catch( Exception e )
		{
			BbUtil.error("Error removing content", e);
			throw Throwables.propagate(e);
		}
	}

	private void removeGradableItem(Content content)
	{
		try
		{
			// Delete gradebook item
			final GradableItemManager gradableItemManager = GradebookManagerFactory
				.getGradableItemManagerWithoutSecurityCheck();
			final GradableItem gradableItem = gradableItemManager.getGradebookItemByContentId(content.getId());
			if( gradableItem != null )
			{
				try
				{
					gradableItemManager.deleteGradebookItem(gradableItem.getId());
				}
				catch( BbSecurityException e )
				{
					BbUtil.error("Error removing gradableItem", e);
					throw Throwables.propagate(e);
				}
			}
		}
		catch( Exception e )
		{
			BbUtil.error("Error removing GradableItem", e);
			throw Throwables.propagate(e);
		}
	}

	private void unregister(ItemKey itemKey)
	{
		try
		{
			final ContentRegisteredResponse reg = RegistrationUtil.contentIsRegistered(itemKey);
			if( reg.isRegistered() )
			{
				BbUtil.debug("Unregistering content " + itemKey + "");
				RegistrationUtil.unrecordItem(reg.getId(), reg.getContentId());
			}
			else
			{
				BbUtil.debug("Content " + itemKey + " is not registered");
			}
		}
		catch( Exception t )
		{
			BbUtil.error("An error occurred while un-registering content", t);
		}
	}

	private void deactivate(ItemInfo itemInfo, String equellaUrl, String token)
	{
		GetMethod method = null;
		try
		{
			final String requestUuid = itemInfo.getActivateRequestUuid();
			if( !Strings.isNullOrEmpty(requestUuid) )
			{
				// FIXME: No option but tokens.... (for now)
				final String url = PathUtils.urlPath(equellaUrl, "access/activationwebservice.do") + "?token="
					+ BbUtil.urlEncode(token) + "&activationUuid=" + BbUtil.urlEncode(requestUuid);
				method = new GetMethod(url);

				final HttpClient client = new HttpClient();
				client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
				client.executeMethod(method.getHostConfiguration(), method, client.getState());
				final String response = method.getResponseBodyAsString();
				if( !response.equals("OK") )
				{
					throw new Exception("Error deactivating attachment: " + response);
				}
			}
		}
		catch( final Exception e )
		{
			BbUtil.error("An error occurred while deactivating content", e);
			throw Throwables.propagate(e);
		}
		finally
		{
			if( method != null )
			{
				method.releaseConnection();
			}
		}
	}

	public boolean isLegacy(Content content)
	{
		final FormattedText body = content.getBody();
		if( body != null && body.getFormattedText().startsWith("<!--") )
		{
			return true;
		}
		return false;
	}

	public ItemInfo ensureProperties(Content content, Course course, Id folderId, String equellaUrl)
	{
		return ItemUtil.getItemInfo(content, course, folderId, equellaUrl);
	}

	private ContentDbLoader getContentLoader()
	{
		if( contentLoader == null )
		{
			BbUtil.trace("Content loader not yet initialised");
			try
			{
				contentLoader = (ContentDbLoader) BbContext.instance().getPersistenceManager()
					.getLoader(ContentDbLoader.TYPE);
			}
			catch( final Exception e )
			{
				BbUtil.error("Error creating content loader", e);
				throw Throwables.propagate(e);
			}
		}
		return contentLoader;
	}

	private ContentDbPersister getContentPersister()
	{
		if( contentPersister == null )
		{
			BbUtil.trace("Content persister not yet initialised");
			try
			{
				contentPersister = (ContentDbPersister) BbContext.instance().getPersistenceManager()
					.getPersister(ContentDbPersister.TYPE);
			}
			catch( final PersistenceException e )
			{
				BbUtil.error("Error creating content persister", e);
				throw Throwables.propagate(e);
			}
		}
		return contentPersister;
	}

	public static class ContentRegisteredResponse
	{
		private int id;
		private boolean registered;
		private boolean available;
		private String contentId;

		public int getId()
		{
			return id;
		}

		public void setId(int id)
		{
			this.id = id;
		}

		public boolean isRegistered()
		{
			return registered;
		}

		public void setRegistered(boolean registered)
		{
			this.registered = registered;
		}

		public boolean isAvailable()
		{
			return available;
		}

		public void setAvailable(boolean available)
		{
			this.available = available;
		}

		public String getContentId()
		{
			return contentId;
		}

		public void setContentId(String contentId)
		{
			this.contentId = contentId;
		}
	}
}
