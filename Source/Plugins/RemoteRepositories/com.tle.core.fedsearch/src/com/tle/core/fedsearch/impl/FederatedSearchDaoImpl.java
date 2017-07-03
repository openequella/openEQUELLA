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

package com.tle.core.fedsearch.impl;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.FederatedSearch;
import com.tle.core.entity.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.fedsearch.FederatedSearchDao;
import com.tle.core.guice.Bind;
import com.tle.core.remoting.RemoteFederatedSearchService;
import com.tle.common.institution.CurrentInstitution;

@Bind(FederatedSearchDao.class)
@Singleton
public class FederatedSearchDaoImpl extends AbstractEntityDaoImpl<FederatedSearch> implements FederatedSearchDao
{
	public FederatedSearchDaoImpl()
	{
		super(FederatedSearch.class);
	}

	@Override
	@SuppressWarnings({"unchecked", "nls"})
	public List<Long> findEngineNamesByType(String type)
	{
		return getHibernateTemplate().findByNamedParam(
			"select id from FederatedSearch where institution = :i and type = :type", new String[]{"i", "type"},
			new Object[]{CurrentInstitution.get(), type});
	}

	@Override
	@SuppressWarnings("nls")
	public List<FederatedSearch> enumerateAllZ3950()
	{
		return findAllByCriteria(Restrictions.eq("institution", CurrentInstitution.get()),
			Restrictions.eq("type", "Z3950SearchEngine"));
	}

	@Override
	public List<BaseEntityLabel> listEnabled()
	{
		return listAll(RemoteFederatedSearchService.ENTITY_TYPE, new ListCallback()
		{

			@Override
			public String getAdditionalJoins()
			{
				return null;
			}

			@Override
			public String getAdditionalWhere()
			{
				return "be.disabled = false";
			}

			@Override
			public String getOrderBy()
			{
				return null;
			}

			@Override
			public void processQuery(Query query)
			{

			}

			@Override
			public boolean isDistinct()
			{
				return false;
			}
		});
	}

}
