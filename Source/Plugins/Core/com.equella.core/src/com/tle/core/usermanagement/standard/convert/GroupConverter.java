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

package com.tle.core.usermanagement.standard.convert;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.user.TLEGroup;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.TreeNodeConverter;
import com.tle.core.usermanagement.standard.dao.TLEGroupDao;

@Bind
@Singleton
public class GroupConverter extends TreeNodeConverter<TLEGroup>
{
	@Inject
	private TLEGroupDao tleGroupDao;

	public GroupConverter()
	{
		super("groups", "groups.xml"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public TLEGroupDao getDao()
	{
		return tleGroupDao;
	}

	@Override
	public ConverterId getConverterId()
	{
		return ConverterId.GROUPS;
	}

	@Override
	public Class<TLEGroup> getNodeClass()
	{
		return TLEGroup.class;
	}
}
