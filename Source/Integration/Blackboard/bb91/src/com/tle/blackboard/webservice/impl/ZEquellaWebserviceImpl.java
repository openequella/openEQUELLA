package com.tle.blackboard.webservice.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import blackboard.data.ExtendedData;
import blackboard.data.content.Content;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;
import blackboard.persist.content.ContentDbLoader;
import blackboard.platform.security.Entitlement;
import blackboard.platform.security.SecurityUtil;
import blackboard.platform.ws.AxisHelpers;
import blackboard.platform.ws.WebserviceContext;
import blackboard.platform.ws.WebserviceException;
import blackboard.platform.ws.anns.AuthenticatedMethod;

import com.tle.blackboard.common.BbUtil;
import com.tle.blackboard.common.content.ContentUtil;
import com.tle.blackboard.common.content.ItemInfo;
import com.tle.blackboard.common.content.ItemKey;
import com.tle.blackboard.common.content.ItemUtil;
import com.tle.blackboard.common.content.LegacyItemUtil;
import com.tle.blackboard.common.content.RegistrationUtil;
import com.tle.blackboard.common.propbag.PropBagMin;
import com.tle.blackboard.webservice.AddItemResult;
import com.tle.blackboard.webservice.Base;
import com.tle.blackboard.webservice.Course;
import com.tle.blackboard.webservice.EquellaWebservice;
import com.tle.blackboard.webservice.Folder;
import com.tle.blackboard.webservice.SearchResult;

/**
 * Why is it called ZEquellaWebserviceImpl? 
 * Unfortunately this class must be the last entry in the zip file, otherwise you are screwed. 
 * It is also not allowed to get too big, otherwise you run into another Blackboard bug.
 * 
 * @author Aaron
 */
@SuppressWarnings("nls")
@WebService(name = "EQUELLA", serviceName = "EQUELLA", portName = "WS", targetNamespace = "http://webservice.blackboard.tle.com")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public class ZEquellaWebserviceImpl implements EquellaWebservice
{
	private static final int CURRENT_EQUELLA_WS_VERSION = 3;
	private static final boolean DEV = false;

	// private static final String THIS_WS_NAME = "EQUELLA";

	/**
	 * Do not use. Required to prevent two Course elements appearing in WSDL.
	 * 
	 * @return A blank Base
	 */
	@Override
	@WebMethod(action = "aBaseReturningMethod")
	@WebResult(name = "base")
	public Base aBaseReturningMethod()
	{
		return new Base();
	}

	@Override
	@WebMethod(action = "getServerVersion")
	@WebResult(name = "serverVersion")
	public int getServerVersion()
	{
		return CURRENT_EQUELLA_WS_VERSION;
	}

	@Override
	@WebMethod(action = "testConnection")
	@WebResult(name = "success")
	public String testConnection(@WebParam(mode = WebParam.Mode.IN, name = "param") String param)
	{
		return param;
	}

	@Override
	@WebMethod(action = "listAllEnrolledCoursesForUser")
	@WebResult(name = "courses")
	public Course[] listCoursesForUser(@WebParam(mode = WebParam.Mode.IN, name = "username") String username,
		@WebParam(mode = WebParam.Mode.IN, name = "archived") boolean archived,
		@WebParam(mode = WebParam.Mode.IN, name = "modifiableOnly") boolean modifiableOnly)
	{
		try
		{
			final BbWsSession session = new BbWsSession(WebserviceContext.getCurrentSession());

			final List<Course> courses = new ArrayList<Course>();
			Set<Id> coursesWithCreate = null;
			if( modifiableOnly )
			{
				coursesWithCreate = SecurityUtil.getCourseIdsWithEntitlement(session.getUserId(), new Entitlement(
					"course.content.CREATE"));
			}

			for( blackboard.data.course.Course bbcourse : WebServiceUtil.getBbCourses(modifiableOnly ? session
				.getUserId() : null) )
			{
				if( !modifiableOnly || canCreate(coursesWithCreate, bbcourse.getId())
					&& (archived || bbcourse.getIsAvailable()) )
				{
					courses.add(WebServiceUtil.convertCourse(bbcourse));
				}
			}

			return courses.toArray(new Course[courses.size()]);
		}
		catch( WebserviceException ws )
		{
			throw ws;
		}
		catch( Exception t )
		{
			chuckIt(t, "EQ005");
			return null;
		}
	}

	private boolean canCreate(Set<Id> coursesWithCreate, Id courseId)
	{
		if( coursesWithCreate == null )
		{
			return true;
		}
		for( Id id : coursesWithCreate )
		{
			if( id.toExternalString().equals(courseId.toExternalString()) )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	@WebMethod(action = "listFoldersForCourse")
	@WebResult(name = "folders")
	public Folder[] listFoldersForCourse(@WebParam(mode = WebParam.Mode.IN, name = "courseId") String courseId)
	{
		try
		{
			final BbWsSession session = new BbWsSession(WebserviceContext.getCurrentSession());
			final List<Folder> folders = new ArrayList<Folder>();
			for( Content folder : BbUtil.getBbFoldersForCourse(BbUtil.getCourseId(courseId)) )
			{
				folders.add(WebServiceUtil.convertFolder(folder, null));
			}
			return folders.toArray(new Folder[folders.size()]);
		}
		catch( WebserviceException ws )
		{
			throw ws;
		}
		catch( Exception t )
		{
			chuckIt(t, "EQ006");
			return null;
		}
	}

	@Override
	@WebMethod(action = "listFoldersForFolder")
	@WebResult(name = "folders")
	public Folder[] listFoldersForFolder(@WebParam(mode = WebParam.Mode.IN, name = "folderId") String folderId)
	{
		try
		{
			final BbWsSession session = new BbWsSession(WebserviceContext.getCurrentSession());
			final List<Folder> folders = new ArrayList<Folder>();
			for( Content courseContent : BbUtil.getBbContentForFolder(BbUtil.getFolderId(folderId)) )
			{
				if( courseContent.getIsFolder() )
				{
					folders.add(WebServiceUtil.convertFolder(courseContent, null));
				}
			}
			return folders.toArray(new Folder[folders.size()]);
		}
		catch( WebserviceException ws )
		{
			throw ws;
		}
		catch( Exception t )
		{
			chuckIt(t, "EQ007");
			return null;
		}
	}

	@Override
	@WebMethod(action = "addItemToCourse")
	@WebResult(name = "folder")
	@AuthenticatedMethod(entitlements = {"course.content.CREATE"}, checkEntitlement = true)
	public AddItemResult addItemToCourse(@WebParam(mode = WebParam.Mode.IN, name = "username") String username,
		@WebParam(mode = WebParam.Mode.IN, name = "courseId") String courseId,
		@WebParam(mode = WebParam.Mode.IN, name = "folderId") String folderId,
		@WebParam(mode = WebParam.Mode.IN, name = "itemUuid") String itemUuid,
		@WebParam(mode = WebParam.Mode.IN, name = "itemVersion") int itemVersion,
		@WebParam(mode = WebParam.Mode.IN, name = "url") String url,
		@WebParam(mode = WebParam.Mode.IN, name = "title") String title,
		@WebParam(mode = WebParam.Mode.IN, name = "description") String description,
		@WebParam(mode = WebParam.Mode.IN, name = "xml") String xml,
		@WebParam(mode = WebParam.Mode.IN, name = "serverUrl") String serverUrl,
		@WebParam(mode = WebParam.Mode.IN, name = "attachment") String attachmentUuid)
	{
		final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
		try
		{
			Thread.currentThread().setContextClassLoader(ContentDbLoader.class.getClassLoader());
			final BbWsSession session = new BbWsSession(WebserviceContext.getCurrentSession());
			final Id courseBbId = BbUtil.getCourseId(courseId);
			final Id folderBbId = BbUtil.getFolderId(folderId);
			final blackboard.data.course.Course bbCourse = session.loadCourse(courseBbId);

			final Set<Id> coursesWithCreate = SecurityUtil.getCourseIdsWithEntitlement(session.getUserId(),
				new Entitlement("course.content.CREATE"));
			if( !canCreate(coursesWithCreate, courseBbId) )
			{
				throw new PermissionException("course.content.CREATE not granted for the user " + username
					+ " and course " + courseId);
			}

			//final String itemUrl = PathUtils.urlPath("integ/gen", itemUuid, Integer.toString(itemVersion)) + url;

			// We need a new endpoint to just pass these 2 values in
			final PropBagMin xmlBag = new PropBagMin(xml);
			final String mimeType = xmlBag.getNode("attachments/@selectedMimeType", null);
			final String attachmentTitle = xmlBag.getNode("attachments/@selectedTitle");

			final String relUrl = ItemUtil.convertToRelativeUrl(serverUrl, url);
			//TODO: need to somehow respect the building block newWindow setting
			ContentUtil.instance().addContent(bbCourse, folderBbId, itemUuid, itemVersion, relUrl, title, description,
				attachmentUuid, attachmentTitle, mimeType, serverUrl, true);

			final Content bbFolder = session.loadContent(folderBbId);
			final Course course = new Course();
			course.setId(bbCourse.getId().toExternalString());
			course.setName(bbCourse.getTitle());

			final AddItemResult result = new AddItemResult();
			result.setFolder(WebServiceUtil.convertFolder(bbFolder, course.getId()));
			result.setCourse(course);
			return result;
		}
		catch( WebserviceException ws )
		{
			throw ws;
		}
		catch( PermissionException p )
		{
			chuckIt(p, "EQ100");
			return null;
		}
		catch( PersistenceException e )
		{
			chuckIt(e, "EQ003");
			return null;
		}
		catch( Exception t )
		{
			chuckIt(t, "EQ004");
			return null;
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(oldLoader);
		}
	}

	@Override
	@WebMethod(action = "findUsages")
	@WebResult(name = "content")
	public SearchResult findUsages(@WebParam(mode = WebParam.Mode.IN, name = "serverUrl") String serverUrl,
		@WebParam(mode = WebParam.Mode.IN, name = "itemUuid") String itemUuid,
		@WebParam(mode = WebParam.Mode.IN, name = "itemVersion") int itemVersion,
		@WebParam(mode = WebParam.Mode.IN, name = "versionIsLatest") boolean versionIsLatest,
		@WebParam(mode = WebParam.Mode.IN, name = "available") boolean available,
		@WebParam(mode = WebParam.Mode.IN, name = "allVersions") boolean allVersions)
	{
		final List<ItemInfo> usages = RegistrationUtil.findUsages(serverUrl, itemUuid, itemVersion, versionIsLatest,
			allVersions, available);
		WebServiceUtil.debug("Found " + usages.size() + " usages of " + itemUuid + "/" + itemVersion
			+ " for institutionUrl " + serverUrl);

		try
		{
			final BbWsSession session = new BbWsSession(WebserviceContext.getCurrentSession());
			final Map<String, Course> courseMap = new HashMap<String, Course>();
			final Map<String, Folder> folderMap = new HashMap<String, Folder>();

			final List<com.tle.blackboard.webservice.Content> contents = new ArrayList<com.tle.blackboard.webservice.Content>();
			for( ItemInfo usage : usages )
			{
				final ItemKey key = usage.getItemKey();
				// if the course or folder no longer exist then forget it, the
				// content can't actually still
				// exist either.
				final Course course = WebServiceUtil.getCourse(session, courseMap, key.getCourseId(), false);
				if( course == null )
				{
					// WebServiceUtil.debug("Unrecording " + key +
					// " because course no longer exists");
					RegistrationUtil.unrecordItem(key.getDatabaseId(), key.getContentId());
					continue;
				}

				final Folder folder = getFolder(session, folderMap, course, key.getFolderId());
				if( folder == null )
				{
					// WebServiceUtil.debug("Unrecording " + key +
					// " because folder no longer exists");
					RegistrationUtil.unrecordItem(key.getDatabaseId(), key.getContentId());
					continue;
				}

				final com.tle.blackboard.webservice.Content content = WebServiceUtil
					.convertUsage(usage, course, folder);
				// WebServiceUtil.debug("Adding content \"" + content +
				// "\" to return values");
				contents.add(content);
			}

			return WebServiceUtil.result(usages.size(), contents, courseMap, folderMap);
		}
		catch( WebserviceException ws )
		{
			throw ws;
		}
		catch( Exception t )
		{
			chuckIt(t, "EQ011");
			return null;
		}
	}

	/**
	 * findAllUsages does not do unrecording etc because it needs to be as fast
	 * as possible
	 */
	@Override
	@WebMethod(action = "findAllUsages")
	@WebResult(name = "results")
	public SearchResult findAllUsages(@WebParam(mode = WebParam.Mode.IN, name = "serverUrl") String serverUrl,
		@WebParam(mode = WebParam.Mode.IN, name = "query") String query,
		@WebParam(mode = WebParam.Mode.IN, name = "courseId") String courseId,
		@WebParam(mode = WebParam.Mode.IN, name = "folderId") String folderId,
		@WebParam(mode = WebParam.Mode.IN, name = "available") boolean available,
		@WebParam(mode = WebParam.Mode.IN, name = "offset") int offset,
		@WebParam(mode = WebParam.Mode.IN, name = "count") int count,
		@WebParam(mode = WebParam.Mode.IN, name = "sortColumn") String sortColumn,
		@WebParam(mode = WebParam.Mode.IN, name = "sortReverse") final boolean sortReverse)
	{
		final List<ItemInfo> usages = RegistrationUtil.findAllUsages(serverUrl, query, courseId, folderId, available,
			sortColumn, sortReverse);

		final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
		try
		{
			Thread.currentThread().setContextClassLoader(ContentDbLoader.class.getClassLoader());
			final BbWsSession session = new BbWsSession(WebserviceContext.getCurrentSession());
			final Map<String, Course> courseMap = new HashMap<String, Course>();
			final Map<String, Folder> folderMap = new HashMap<String, Folder>();

			final List<com.tle.blackboard.webservice.Content> contents = new ArrayList<com.tle.blackboard.webservice.Content>();
			int index = 0;
			for( ItemInfo usage : usages )
			{
				final ItemKey key = usage.getItemKey();
				// if the course or folder no longer exist then forget it, the
				// content can't actually still
				// exist either.
				final Course course = WebServiceUtil.getCourse(session, courseMap, key.getCourseId(), true);
				if( course == null )
				{
					continue;
				}

				final Folder folder = getFolder(session, folderMap, course, key.getFolderId());
				if( folder == null )
				{
					continue;
				}

				if( index >= offset && (count < 0 || index < offset + count) )
				{
					final com.tle.blackboard.webservice.Content content = WebServiceUtil.convertUsage(usage, course,
						folder);
					// WebServiceUtil.debug("Adding content \"" + content +
					// "\" to return values");
					contents.add(content);
				}
				index++;
				if( count >= 0 && index >= offset + count )
				{
					break;
				}
			}

			// gone past the end of list
			if( contents.size() == 0 )
			{
				final SearchResult res = new SearchResult();
				res.setAvailable(usages.size());
				return res;
			}

			return WebServiceUtil.result(usages.size(), contents, courseMap, folderMap);
		}
		catch( WebserviceException ws )
		{
			throw ws;
		}
		catch( Exception t )
		{
			chuckIt(t, "EQ011");
			return null;
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(oldLoader);
		}
	}

	private Folder getFolder(BbWsSession session, Map<String, Folder> folderMap, Course course, String folderId)
		throws PersistenceException
	{
		Folder folder = folderMap.get(folderId);
		if( folder == null )
		{
			// WebServiceUtil.debug("Folder " + folderId +
			// " not found in map, loading it");
			Content bbFolder = session.loadContent(folderId);
			if( bbFolder != null )
			{
				folder = WebServiceUtil.convertFolder(bbFolder, course.getId());
				folderMap.put(folderId, folder);
			}
			else
			{
				WebServiceUtil.debug("Folder could not be loaded.  It doesn't exist any more!");
			}
		}
		return folder;
	}

	@Override
	@WebMethod(action = "synchroniseEquellaContentTables")
	@WebResult(name = "success")
	public boolean synchroniseEquellaContentTables(
		@WebParam(mode = WebParam.Mode.IN, name = "institutionUrl") String institutionUrl,
		@WebParam(mode = WebParam.Mode.IN, name = "available") boolean available)
	{
		WebServiceUtil.debug("synchroniseEquellaContentTables(" + institutionUrl + ")");
		new SynchroniseContentThread(institutionUrl, available).start();
		return true;
	}

	@Override
	@WebMethod(action = "getCourseCode")
	@WebResult(name = "code")
	public String getCourseCode(@WebParam(mode = WebParam.Mode.IN, name = "courseId") String courseId)
	{
		try
		{
			final BbWsSession session = new BbWsSession(WebserviceContext.getCurrentSession());
			final blackboard.data.course.Course course = session.loadCourse(courseId);
			return course.getCourseId();
		}
		catch( WebserviceException ws )
		{
			throw ws;
		}
		catch( Exception t )
		{
			chuckIt(t, "EQ012");
			return null;
		}
	}

	@Override
	@WebMethod(action = "deleteContent")
	@WebResult(name = "success")
	public boolean deleteContent(@WebParam(mode = WebParam.Mode.IN, name = "contentId") String contentId)
	{
		final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
		try
		{
			Thread.currentThread().setContextClassLoader(ContentDbLoader.class.getClassLoader());
			final BbWsSession session = new BbWsSession(WebserviceContext.getCurrentSession());
			if( contentId != null )
			{
				final Content content = session.loadContent(contentId);
				ensurePermission(session, content.getCourseId(), "course.content.DELETE");

				session.deleteContent(contentId);
				RegistrationUtil.unrecordItem(0, contentId);
			}
			else
			{
				// unsupported, they need to run the scheduled task
				throw new RuntimeException(
					"Please run the Synchronise Blackboard Content scheduled task with the latest Building Block and Web service installed");
			}
			return true;
		}
		catch( WebserviceException ws )
		{
			throw ws;
		}
		catch( Exception t )
		{
			chuckIt(t, "EQ013");
			return false;
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(oldLoader);
		}
	}

	@Override
	@WebMethod(action = "editContent")
	@WebResult(name = "success")
	public boolean editContent(@WebParam(mode = WebParam.Mode.IN, name = "contentId") String contentId,
		@WebParam(mode = WebParam.Mode.IN, name = "title") String title,
		@WebParam(mode = WebParam.Mode.IN, name = "description") String description,
		@WebParam(mode = WebParam.Mode.IN, name = "institutionUrl") String institutionUrl)
	{
		final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
		try
		{
			Thread.currentThread().setContextClassLoader(ContentDbLoader.class.getClassLoader());
			final BbWsSession session = new BbWsSession(WebserviceContext.getCurrentSession());
			final Content content = session.loadContent(contentId);
			ensurePermission(session, content.getCourseId(), "course.content.MODIFY");

			final String html;
			if( ContentUtil.instance().isLegacy(content) )
			{
				final String body = content.getBody().getFormattedText();
				final PropBagMin xml = new PropBagMin(LegacyItemUtil.extractXmlFromBody(body));
				xml.setNode("name", title);
				xml.setNode("description", description);
				html = LegacyItemUtil.getHtml(institutionUrl, xml.toString());
			}
			else
			{
				final ExtendedData extendedData = content.getExtendedData();
				extendedData.setValue("description", description);
				html = ItemUtil.getHtml(institutionUrl, extendedData.getValue("url"),
					extendedData.getValue("attachmentName"), extendedData.getValue("mimeType"), title, description,
					content.getLaunchInNewWindow());
			}

			WebServiceUtil.modifyContent(session, content, null, null, title, description, html);

			return true;
		}
		catch( WebserviceException ws )
		{
			throw ws;
		}
		catch( Exception t )
		{
			chuckIt(t, "EQ014");
			return false;
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(oldLoader);
		}
	}

	@Override
	@WebMethod(action = "moveContent")
	@WebResult(name = "success")
	public boolean moveContent(@WebParam(mode = WebParam.Mode.IN, name = "contentId") String contentId,
		@WebParam(mode = WebParam.Mode.IN, name = "courseId") String courseId,
		@WebParam(mode = WebParam.Mode.IN, name = "folderId") String folderId)
	{
		final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
		try
		{
			Thread.currentThread().setContextClassLoader(ContentDbLoader.class.getClassLoader());
			final BbWsSession session = new BbWsSession(WebserviceContext.getCurrentSession());
			// Need to ensure privs for source and target course
			final Content content = session.loadContent(contentId);
			ensurePermission(session, content.getCourseId(), "course.content.DELETE");
			ensurePermission(session, session.getCourseId(courseId), "course.content.CREATE");

			WebServiceUtil.modifyContent(session, content, courseId, folderId, null, null, null);
			return true;
		}
		catch( WebserviceException ws )
		{
			throw ws;
		}
		catch( Exception t )
		{
			chuckIt(t, "EQ015");
			return false;
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(oldLoader);
		}
	}

	private void ensurePermission(BbWsSession session, Id courseId, String permission)
	{
		if( !SecurityUtil.userHasEntitlement(session.getUserId(), courseId, new Entitlement(permission)) )
		{
			chuckIt(
				new RuntimeException("User does not have " + permission + " entitlement for course "
					+ courseId.toExternalString()), "EQ100");
		}
	}

	private void chuckIt(Throwable t, String code)
	{
		WebServiceUtil.error("Error occurred", t);
		String message = t.getMessage();
		if( DEV )
		{
			StringWriter sw = new StringWriter();
			PrintWriter w = new PrintWriter(sw);
			t.printStackTrace(w);
			message += "<pre>" + sw.toString() + "</pre>";
		}
		AxisHelpers.throwWSException(code, message);
	}
}
