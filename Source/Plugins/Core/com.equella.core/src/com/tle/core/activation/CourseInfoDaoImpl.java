/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.activation;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.core.entity.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.common.institution.CurrentInstitution;

/**
 * @author Charles O'Farrell
 */
@Bind(CourseInfoDao.class)
@Singleton
public class CourseInfoDaoImpl extends AbstractEntityDaoImpl<CourseInfo> implements CourseInfoDao
{
	public CourseInfoDaoImpl()
	{
		super(CourseInfo.class);
	}

	@Override
	@SuppressWarnings({"unchecked", "nls"})
	public List<String> getAllCitations()
	{
		return getHibernateTemplate().findByNamedParam(
			"select distinct c.name from Schema s join s.citations c where s.institution = :inst", "inst",
			CurrentInstitution.get());
	}

	@Override
	@SuppressWarnings({"unchecked", "nls"})
	public List<Class<?>> getReferencingClasses(long id)
	{
		List<Class<?>> classes = new ArrayList<Class<?>>();

		if( ((List<Long>) getHibernateTemplate().findByNamedParam(
			"select count(*) from ActivateRequest a where a.course.id = :id", "id", id)).get(0) != 0 )
		{
			classes.add(ActivateRequest.class);
		}
		return classes;
	}
}
