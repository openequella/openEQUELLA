/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.core.activation.service;

import java.util.List;

import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.util.CsvReader;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.remoting.RemoteCourseInfoService;

public interface CourseInfoService extends RemoteCourseInfoService, AbstractEntityService<EntityEditingBean, CourseInfo>
{
	String PRIV_CREATE_COURSE = "CREATE_COURSE_INFO";
	String PRIV_EDIT_COURSE = "EDIT_COURSE_INFO";

	void edit(CourseInfo course);

	CourseInfo getByCode(String code);

	List<CourseInfo> bulkImport(CsvReader reader, boolean override);
}