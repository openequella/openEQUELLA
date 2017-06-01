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

package com.tle.core.institution.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.security.AccessEntry;
import com.tle.common.Check;
import com.tle.core.dao.AclDao;
import com.tle.core.guice.Bind;
import com.tle.core.institution.AclService;

@Bind(AclService.class)
@Singleton
public class AclServiceImpl implements AclService
{
	@Inject
	private AclDao aclDao;

	@Override
	public List<AccessEntry> listAll()
	{
		List<AccessEntry> allAcls = aclDao.listAll();
		return allAcls;
	}

	@Override
	public void saveAll(List<AccessEntry> acls)
	{
		if( !Check.isEmpty(acls) )
		{
			for( AccessEntry ae : acls )
			{
				aclDao.save(ae);
			}
			aclDao.flush();
			aclDao.clear();
		}
	}

}
