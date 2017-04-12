package com.tle.blackboard.webservice.impl;

import blackboard.data.ValidationException;
import blackboard.data.content.Content;
import blackboard.data.course.Course;
import blackboard.data.user.User;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.content.ContentDbLoader;
import blackboard.persist.content.ContentDbPersister;
import blackboard.persist.course.CourseDbLoader;
import blackboard.persist.course.CourseDbPersister;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.user.UserDbLoader;
import blackboard.platform.ws.SessionVO;

import com.tle.blackboard.common.BbContext;
import com.tle.blackboard.common.BbUtil;

/**
 * Container for various webservice session info
 * 
 * @author Aaron
 */
public class BbWsSession
{
	private final SessionVO session;
	// lazy fields
	private ContentDbLoader contentDbLoader;
	private ContentDbPersister contentDbPersister;
	private CourseDbLoader courseDbLoader;
	private CourseDbPersister courseDbPersister;
	private CourseMembershipDbLoader membershipDbLoader;
	private UserDbLoader userDbLoader;

	public BbWsSession(SessionVO session)
	{
		this.session = session;
	}

	public Id getUserId()
	{
		return BbUtil.getUserId(session.getUserId());
	}

	public Id getContentId(String contentId)
	{
		return BbUtil.getContentId(contentId);
	}

	public Id getCourseId(String courseId)
	{
		return BbUtil.getCourseId(courseId);
	}

	// Content methods

	public ContentDbLoader getContentDbLoader() throws PersistenceException
	{
		if( contentDbLoader == null )
		{
			contentDbLoader = (ContentDbLoader) BbContext.instance().getPersistenceManager()
				.getLoader(ContentDbLoader.TYPE);
		}
		return contentDbLoader;
	}

	public ContentDbPersister getContentDbPersister() throws PersistenceException
	{
		if( contentDbPersister == null )
		{
			contentDbPersister = (ContentDbPersister) BbContext.instance().getPersistenceManager()
				.getPersister(ContentDbPersister.TYPE);
		}
		return contentDbPersister;
	}

	public void persist(Content content) throws PersistenceException, ValidationException
	{
		getContentDbPersister().persist(content);
	}

	public Content loadContent(String contentId) throws PersistenceException
	{
		return BbUtil.loadContent(getContentDbLoader(), contentId);
	}

	public Content loadContent(Id contentId) throws PersistenceException
	{
		return BbUtil.loadContent(getContentDbLoader(), contentId);
	}

	public void deleteContent(String contentId) throws PersistenceException
	{
		getContentDbPersister().deleteById(BbUtil.getContentId(contentId));
	}

	// Course methods

	public CourseDbLoader getCourseDbLoader() throws PersistenceException
	{
		if( courseDbLoader == null )
		{
			courseDbLoader = (CourseDbLoader) BbContext.instance().getPersistenceManager()
				.getLoader(CourseDbLoader.TYPE);
		}
		return courseDbLoader;
	}

	public CourseDbPersister getCourseDbPersister() throws PersistenceException
	{
		if( courseDbPersister == null )
		{
			courseDbPersister = (CourseDbPersister) BbContext.instance().getPersistenceManager()
				.getPersister(CourseDbPersister.TYPE);
		}
		return courseDbPersister;
	}

	public void persist(Course course) throws PersistenceException, ValidationException
	{
		getCourseDbPersister().persist(course);
	}

	public Course loadCourse(String courseId) throws PersistenceException
	{
		return BbUtil.loadCourse(getCourseDbLoader(), courseId);
	}

	public Course loadCourse(Id courseId) throws PersistenceException
	{
		return BbUtil.loadCourse(getCourseDbLoader(), courseId);
	}

	// Membership methods

	public CourseMembershipDbLoader getMembershipDbLoader() throws PersistenceException
	{
		if( membershipDbLoader == null )
		{
			membershipDbLoader = (CourseMembershipDbLoader) BbContext.instance().getPersistenceManager()
				.getLoader(CourseMembershipDbLoader.TYPE);
		}
		return membershipDbLoader;
	}

	// User methods

	public UserDbLoader getUserDbLoader() throws PersistenceException
	{
		if( userDbLoader == null )
		{
			userDbLoader = (UserDbLoader) BbContext.instance().getPersistenceManager().getLoader(UserDbLoader.TYPE);
		}
		return userDbLoader;
	}

	public User loadUser(String userId) throws PersistenceException
	{
		return loadUser(BbUtil.getUserId(userId));
	}

	public User loadUser(Id userId) throws PersistenceException
	{
		try
		{
			return getUserDbLoader().loadById(userId);
		}
		// KNF..uh huh uh huh uh huh. KNF's gunna rock ya
		// http://www.youtube.com/watch?v=LXEOESuiYcA
		catch( KeyNotFoundException knf )
		{
			return null;
		}
	}
}
