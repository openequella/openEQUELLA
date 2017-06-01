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

package com.tle.core.activation.validation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tle.beans.activation.ActivateRequest;

public class ActivationPeriodHelper
{
	private final List<ActivateRequest> requests;

	public ActivationPeriodHelper(List<ActivateRequest> requests)
	{
		this.requests = requests;
	}

	public Set<Date> calculatePoints()
	{
		Set<Date> points = new HashSet<Date>();
		for( ActivateRequest request : requests )
		{
			if( request.getFrom() != null && request.getUntil() != null )
			{
				points.add(request.getFrom());
				points.add(request.getUntil());
			}
		}
		return points;
	}

	/**
	 * Collection of reports which contain the given date.
	 */
	public List<ActivateRequest> calculateIntersections(Date time)
	{
		List<ActivateRequest> intersects = new ArrayList<ActivateRequest>();
		for( ActivateRequest report : requests )
		{
			if( report.getFrom() != null && report.getUntil() != null )
			{
				if( !(report.getFrom().after(time) || report.getUntil().before(time)) )
				{
					intersects.add(report);
				}
			}
		}

		return intersects;
	}

}
