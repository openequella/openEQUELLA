package com.tle.core.activation;

import java.util.List;

import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.core.entity.dao.AbstractEntityDao;

/**
 * @author Charles O'Farrell
 */
public interface CourseInfoDao extends AbstractEntityDao<CourseInfo>
{
	List<String> getAllCitations();

	List<Class<?>> getReferencingClasses(long id);
}
