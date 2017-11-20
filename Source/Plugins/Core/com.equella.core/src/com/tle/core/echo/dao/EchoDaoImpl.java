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

package com.tle.core.echo.dao;

import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.tle.beans.Institution;
import com.tle.core.echo.entity.EchoServer;
import com.tle.core.entity.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;

@Bind(EchoDao.class)
@Singleton
@SuppressWarnings("nls")
public class EchoDaoImpl extends AbstractEntityDaoImpl<EchoServer> implements EchoDao
{
	public EchoDaoImpl()
	{
		super(EchoServer.class);
	}

	@Override
	public EchoServer getBySystemID(Institution inst, String esid)
	{
		return findByCriteria(Restrictions.eq("echoSystemID", esid), Restrictions.eq("institution", inst));
	}
}