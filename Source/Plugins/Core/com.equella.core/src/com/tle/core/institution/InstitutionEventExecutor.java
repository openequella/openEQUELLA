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

package com.tle.core.institution;

import java.text.MessageFormat;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.core.events.EventExecutor;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.RunAsInstitution;

@Singleton
@Bind(EventExecutor.class)
@SuppressWarnings("nls")
public class InstitutionEventExecutor implements EventExecutor
{
	@Inject
	private InstitutionService institutionService;
	@Inject
	private RunAsInstitution runAs;

	@Override
	public Runnable createRunnable(final long institutionId, final Runnable runnable)
	{
		if( institutionId < 0 )
		{
			return runnable;
		}

		return new Runnable()
		{
			@Override
			public void run()
			{
				Institution institution = institutionService.getInstitution(institutionId);
				if( institution != null )
				{
					runAs.executeAsSystem(institution, runnable);
				}
				else
				{
					throw new RuntimeException(
						MessageFormat.format("Institution for ID: {0} not found", institutionId));
				}
			}
		};
	}
}
