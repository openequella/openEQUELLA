package com.tle.blackboard.buildingblock.data;

import java.util.Calendar;
import java.util.Date;

import blackboard.data.course.Course;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseDbLoader;

import com.google.common.base.Throwables;
import com.tle.blackboard.common.BbContext;
import com.tle.blackboard.common.BbUtil;

@SuppressWarnings("nls")
// @NonNullByDefault
public class WrappedCourse
{
	private final Id id;
	/* @Nullable */
	private Course course;
	/* @Nullable */
	private CourseDbLoader courseLoader;

	public WrappedCourse(String courseId)
	{
		this(BbUtil.getCourseId(courseId));
	}

	public WrappedCourse(Id courseId)
	{
		this.id = courseId;
	}

	@SuppressWarnings("null")
	public WrappedCourse(Course course)
	{
		this.id = course.getId();
		this.course = course;
	}

	public Date getStartDate()
	{
		Calendar date = null;
		try
		{
			final Course course = getCourse();
			date = course.getStartDate();
		}
		catch( final Exception e )
		{
			BbUtil.error("Error loading course: " + getId(), e);
			date = Calendar.getInstance();
		}
		if( date == null )
		{
			date = Calendar.getInstance();
		}
		return date.getTime();
	}

	public Date getEndDate()
	{
		Calendar date = null;
		try
		{
			final Course course = getCourse();
			date = course.getEndDate();
		}
		catch( final Exception e )
		{
			BbUtil.error("Error loading course: " + getId(), e);
		}
		if( date == null )
		{
			date = Calendar.getInstance();
		}
		return date.getTime();
	}

	public boolean isAvailable()
	{
		return getCourse().getIsAvailable();
	}

	@SuppressWarnings("null")
	public synchronized Course getCourse()
	{
		if( course == null )
		{
			try
			{
				course = getCourseLoader().loadById(getId());
			}
			catch( final Exception e )
			{
				throw Throwables.propagate(e);
			}
		}
		return course;
	}

	@SuppressWarnings("null")
	private CourseDbLoader getCourseLoader()
	{
		if( courseLoader == null )
		{
			try
			{
				this.courseLoader = (CourseDbLoader) BbContext.instance().getPersistenceManager()
					.getLoader(CourseDbLoader.TYPE);
			}
			catch( final PersistenceException e )
			{
				throw Throwables.propagate(e);
			}
		}
		return courseLoader;
	}

	@SuppressWarnings("null")
	public String getTitle()
	{
		return getCourse().getTitle();
	}

	@SuppressWarnings("null")
	public String getCode()
	{
		return getCourse().getCourseId();
	}

	public Id getId()
	{
		return id;
	}

	@Override
	public String toString()
	{
		return "WrappedCourse { id=" + id + " }";
	}
}