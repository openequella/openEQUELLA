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
