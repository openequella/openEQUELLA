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

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.SystemUserState;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.CurrentDataSource;
import com.tle.core.hibernate.DataSourceHolder;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.system.service.SchemaDataSourceService;

/**
 * @author Nicholas Read
 */
@Bind(RunAsInstitution.class)
@Singleton
public class RunAsInstitutionImpl implements RunAsInstitution
{
	@Inject
	private InstitutionService institutionService;
	@Inject
	private SchemaDataSourceService databaseSchemaService;

	@Override
	public <V> V execute(UserState state, Callable<V> runnable)
	{
		final UserState originalUser = CurrentUser.getUserState();
		final Institution originalInstitution = CurrentInstitution.get();
		final DataSourceHolder originalDataSource = CurrentDataSource.get();

		try
		{
			Institution institution = state.getInstitution();
			CurrentInstitution.set(institution);
			if( institution == null )
			{
				CurrentDataSource.set(null);
			}
			else
			{
				CurrentDataSource.set(databaseSchemaService
					.getDataSourceForId(institutionService.getSchemaIdForInstitution(institution)));
			}
			CurrentUser.setUserState(state);
			return runnable.call();
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
		finally
		{
			CurrentUser.setUserState(originalUser);
			CurrentDataSource.set(originalDataSource);
			CurrentInstitution.set(originalInstitution);
		}
	}

	@Override
	public <V> V executeAsSystem(Institution institution, Callable<V> callable)
	{
		return execute(new SystemUserState(institution), callable);
	}

	@Override
	public void executeAsSystem(Institution institution, Runnable runnable)
	{
		execute(new SystemUserState(institution), Executors.callable(runnable));
	}
}
