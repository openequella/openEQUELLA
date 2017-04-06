/*
 * Created on 18/04/2006
 */
package com.tle.core.remoting;

import java.util.List;

import com.tle.beans.item.cal.request.CourseInfo;

public interface RemoteCourseInfoService extends RemoteAbstractEntityService<CourseInfo>
{
	String ENTITY_TYPE = "COURSE_INFO"; //$NON-NLS-1$

	List<String> getAllCitations();

	List<CourseInfo> bulkImport(byte[] file, boolean override);
}
