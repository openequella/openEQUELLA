package com.tle.blackboard.webservice.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import blackboard.base.FormattedText;
import blackboard.data.AttributePermission;
import blackboard.data.ExtendedData;
import blackboard.data.ValidationException;
import blackboard.data.content.Content;
import blackboard.data.course.CourseMembership;
import blackboard.data.course.CourseMembership.Role;
import blackboard.data.user.User;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseDbLoader;
import blackboard.platform.ws.AxisHelpers;
import blackboard.platform.ws.WebserviceLogger;

import com.google.common.base.Strings;
import com.tle.blackboard.common.BbContext;
import com.tle.blackboard.common.BbUtil;
import com.tle.blackboard.common.content.ItemInfo;
import com.tle.blackboard.common.content.ItemKey;
import com.tle.blackboard.common.content.RegistrationUtil;
import com.tle.blackboard.webservice.Course;
import com.tle.blackboard.webservice.Folder;
import com.tle.blackboard.webservice.SearchResult;

/**
 * This class was created SOLELY to reduce the size of EquellaWebserviceImpl
 * (Blackboard's bug
 * https://behind.blackboard.com/s/sysadminas/support/casedetails
 * .aspx?caseid=872695 )
 * 
 * @author Aaron
 */
@SuppressWarnings("nls")
public class WebServiceUtil
{
	private static final WebserviceLogger LOGGER = WebserviceLogger.getInstance();

	static void error(String text, Throwable t)
	{
		LOGGER.logError(text, t);
	}

	static void debug(String text)
	{
		LOGGER.logDebug(text);
	}

	static Course convertCourse(blackboard.data.course.Course bbcourse)
	{
		final Course c = new Course();
		final String courseId = bbcourse.getId().toExternalString();
		c.setId(courseId);
		c.setName(bbcourse.getTitle());
		c.setCreatedDate(bbcourse.getCreatedDate().getTimeInMillis());
		c.setModifiedDate(bbcourse.getModifiedDate().getTimeInMillis());
		c.setAvailable(bbcourse.getIsAvailable());
		c.setCode(bbcourse.getCourseId());
		c.setUrl("webapps/portal/frameset.jsp?url="
			+ basicUrlEncode("/webapps/blackboard/execute/launcher?type=Course&id=" + courseId));
		return c;
	}

	static Folder convertFolder(Content bbFolder, String courseId)
	{
		final Folder f = new Folder();
		f.setId(bbFolder.getId().getExternalString());
		f.setName(bbFolder.getTitle());

		Calendar modDate = bbFolder.getModifiedDate();
		if( modDate != null )
		{
			f.setModifiedDate(modDate.getTimeInMillis());
		}

		// Apparently createdDate is unreliable hence why it is deprecated
		Calendar createDate = bbFolder.getCreatedDate();
		if( createDate != null )
		{
			f.setCreatedDate(createDate.getTimeInMillis());
		}

		f.setCourseId(courseId);
		return f;
	}

	static Course getCourse(final BbWsSession session, Map<String, Course> courseMap, String courseId,
		boolean fullDetails) throws PersistenceException
	{
		Course course = courseMap.get(courseId);
		if( course == null )
		{
			debug("Course " + courseId + " not found in map, loading it");
			blackboard.data.course.Course bbCourse = session.loadCourse(courseId);
			if( bbCourse != null )
			{
				Id bbCourseId = BbUtil.getCourseId(courseId);

				course = convertCourse(bbCourse);

				if( fullDetails )
				{
					List<String> instructors = new ArrayList<String>();

					List<CourseMembership> members = session.getMembershipDbLoader().loadByCourseIdAndRole(bbCourseId,
						Role.INSTRUCTOR);
					if( members != null )
					{
						debug("Found " + members.size() + " members that are instructors");
						for( CourseMembership membership : members )
						{
							final Id userId = membership.getUserId();
							if( userId != null )
							{
								try
								{
									final Permissions permissions = new Permissions();
									permissions.add(new AttributePermission("User.PersonalInfo", "get"));
									permissions.add(new AttributePermission("User.AuthInfo", "get"));
									final AccessControlContext context = new AccessControlContext(
										new ProtectionDomain[]{new ProtectionDomain(null, permissions)});

									final String username = AccessController.doPrivileged(
										new PrivilegedAction<String>()
										{
											@Override
											public String run()
											{
												try
												{
													final User user = session.loadUser(userId);
													return user.getGivenName() + " " + user.getFamilyName();
												}
												catch( PersistenceException p )
												{
													return null;
												}
												catch( SecurityException sec )
												{
													return null;
												}
											}
										}, context);

									if( username != null )
									{
										instructors.add(username);
									}
								}
								catch( Exception sec )
								{
									//Nothing
								}
							}
							else
							{
								debug("UserId for membership was null");
							}
						}
					}
					else
					{
						debug("Instructors members was null");
					}
					course.setInstructors(instructors.toArray(new String[instructors.size()]));

					// TODO: is there a better way of getting a count only?
					final List<CourseMembership> students = session.getMembershipDbLoader().loadByCourseIdAndRole(
						bbCourseId, Role.STUDENT);
					debug("Found " + students.size() + " members that are students");
					course.setEnrollments(students.size());
				}

				courseMap.put(courseId, course);
			}
			else
			{
				debug("Course could not be loaded.  It doesn't exist any more!");
			}
		}
		return course;
	}

	static Iterable<blackboard.data.course.Course> getBbCourses(Id userId)
	{
		try
		{
			final CourseDbLoader courseDbLoader = (CourseDbLoader) BbContext.instance().getPersistenceManager()
				.getLoader(CourseDbLoader.TYPE);
			return (userId == null ? courseDbLoader.loadAllCourses() : courseDbLoader.loadByUserId(userId));
		}
		catch( Exception t )
		{
			AxisHelpers.throwWSException("EQ005", t.getMessage());
			return null;
		}
	}

	static com.tle.blackboard.webservice.Content convertUsage(ItemInfo usage, Course course, Folder folder)
	{
		final ItemKey key = usage.getItemKey();
		final com.tle.blackboard.webservice.Content content = new com.tle.blackboard.webservice.Content();
		content.setCourseId(course.getId());
		content.setFolderId(folder.getId());
		content.setId(key.getContentId());
		content.setUsageId(key.getDatabaseId());
		content.setUuid(key.getUuid());
		content.setVersion(key.getVersion());
		content.setPage(usage.getAttachmentName());
		content.setCreatedDate(usage.getCreatedDate().getTime());
		content.setModifiedDate(usage.getModifiedDate().getTime());
		content.setAvailable(usage.isAvailable() && course.isAvailable());
		content.setTitle(usage.getName());
		content.setDescription(usage.getDescription());
		Date dateAccessed = usage.getDateAccessed();
		if( dateAccessed != null )
		{
			content.setDateAccessed(dateAccessed.getTime());
		}
		else
		{
			content.setDateAccessed(0);
		}

		// Content URL or folder URL?
		// Ugh
		String courseId = course.getId();
		String url = "webapps/portal/frameset.jsp?url="
			+ basicUrlEncode("/webapps/blackboard/content/listContentEditable.jsp?content_id=" + content.getFolderId()
				+ "&course_id=" + courseId);
		content.setFolderUrl(url.toString());

		return content;
	}

	static String basicUrlEncode(String url)
	{
		try
		{
			return URLEncoder.encode(url, "UTF-8");
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException(e);
		}
	}

	static void modifyContent(BbWsSession session, Content content, String courseId, String folderId, String title,
		String description, String html) throws PersistenceException, ValidationException
	{
		if( content == null )
		{
			// unsupported, they need to run the scheduled task
			throw new RuntimeException(
				"Please run the Synchronise Blackboard Content scheduled task with the latest Building Block and Web service installed");
		}

		final String contentId = content.getId().toExternalString();

		if( !Strings.isNullOrEmpty(courseId) )
		{
			content.setCourseId(BbUtil.getCourseId(courseId));
			content.setParentId(BbUtil.getContentId(folderId));
			RegistrationUtil.updateRecordedLocation(contentId, courseId, folderId);
		}
		boolean modText = false;
		if( !Strings.isNullOrEmpty(title) )
		{
			content.setTitle(title);
			modText = true;
		}
		if( !Strings.isNullOrEmpty(description) )
		{
			final FormattedText text = new FormattedText(html, FormattedText.Type.HTML);
			content.setBody(text);
			modText = true;

			ExtendedData extendedData = content.getExtendedData();
			if( extendedData == null )
			{
				extendedData = new ExtendedData();
				content.setExtendedData(extendedData);
			}
			extendedData.setValue("description", description);
		}
		if( modText )
		{
			RegistrationUtil.updateRecordedTitle(contentId, title, description);
		}

		session.persist(content);
	}

	static SearchResult result(int available, Collection<com.tle.blackboard.webservice.Content> contents,
		Map<String, com.tle.blackboard.webservice.Course> courseMap,
		Map<String, com.tle.blackboard.webservice.Folder> folderMap)
	{
		final SearchResult result = new SearchResult();
		result.setAvailable(available);
		result.setResults(contents.toArray(new com.tle.blackboard.webservice.Content[contents.size()]));

		final Collection<Course> courseCol = courseMap.values();
		result.setCourses(courseCol.toArray(new com.tle.blackboard.webservice.Course[courseCol.size()]));

		final Collection<Folder> folderCol = folderMap.values();
		result.setFolders(folderCol.toArray(new com.tle.blackboard.webservice.Folder[folderCol.size()]));

		return result;
	}
}
