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
