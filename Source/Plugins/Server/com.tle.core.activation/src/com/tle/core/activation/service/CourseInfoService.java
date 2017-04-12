/*
 * Created on 14/12/2005
 */

package com.tle.core.activation.service;

import java.util.List;

import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.util.CsvReader;
import com.tle.core.remoting.RemoteCourseInfoService;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.EntityEditingBean;

public interface CourseInfoService
	extends
		RemoteCourseInfoService,
		AbstractEntityService<EntityEditingBean, CourseInfo>
{
	void edit(CourseInfo course);

	CourseInfo getByCode(String code);

	List<CourseInfo> bulkImport(CsvReader reader, boolean override);
}