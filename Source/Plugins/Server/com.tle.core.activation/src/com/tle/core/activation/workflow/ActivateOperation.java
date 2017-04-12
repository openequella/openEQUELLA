package com.tle.core.activation.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import com.dytech.edge.common.valuebean.ValidationError;
import com.dytech.edge.exceptions.InvalidDataException;
import com.dytech.edge.exceptions.NotFoundException;
import com.google.inject.assistedinject.Assisted;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.Item;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.activation.ActivateRequestDao;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.user.CurrentUser;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

/**
 * @author aholland
 */
@SecureOnCall(priv = "COPYRIGHT_ITEM")
public class ActivateOperation extends AbstractWorkflowOperation
{
	protected List<ActivateRequest> requests;
	private String activationType;
	private boolean ignoreOverrides;
	private boolean skipPercentage;

	@Inject
	private ActivationService activationService;
	@Inject
	private ActivateRequestDao dao;
	@Inject
	private CourseInfoService courseInfoService;

	private static final PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(ActivateOperation.class);

	@Inject
	public ActivateOperation(@Assisted String activationType)
	{
		this.activationType = activationType;
	}

	public void setRequests(List<ActivateRequest> requests)
	{
		this.requests = requests;
	}

	public void setIgnoreOverrides(boolean ignoreOverrides)
	{
		this.ignoreOverrides = ignoreOverrides;
	}

	public void setSkipPercentage(boolean skipPercentage)
	{
		this.skipPercentage = skipPercentage;
	}

	@SuppressWarnings("nls")
	@Override
	public boolean execute()
	{
		Item item = getItem();
		List<ValidationError> errors = new ArrayList<ValidationError>();
		for( ActivateRequest request : requests )
		{
			request.setUser(CurrentUser.getUserID());
			request.setType(activationType);
			request.setItem(item);
			validateDates(errors, request);
			if( !errors.isEmpty() )
			{
				throw new InvalidDataException(errors);
			}
			CourseInfo course = request.getCourse();
			CourseInfo current = courseInfoService.getByCode(course.getCode());
			if( current == null )
			{
				throw new NotFoundException("Couldn't find course");
			}
			request.setCourse(current);
			request.setTime(params.getDateNow());
			// Allow caller - esp API - to set their own uuid
			if( Check.isEmpty(request.getUuid()) )
			{
				request.setUuid(UUID.randomUUID().toString());
			}
			request.setStatus(ActivateRequest.TYPE_PENDING);
			request.setDescription(activationService.getActivationDescription(request));
			dao.save(request);
		}

		activationService.validateItem(activationType, item, ignoreOverrides, skipPercentage);

		return true;
	}

	@SuppressWarnings("nls")
	private void validateDates(List<ValidationError> errors, ActivateRequest request)
	{
		if( request.getFrom() == null )
		{
			errors.add(new ValidationError("from", CurrentLocale.get(urlHelper.key("activate.error.invalidfrom"))));
		}
		else if( request.getUntil() == null )
		{
			errors.add(new ValidationError("until", CurrentLocale.get(urlHelper.key("activate.error.invaliduntil"))));
		}
		else if( request.getFrom().after(request.getUntil()) )
		{
			errors
				.add(new ValidationError("fromuntil", CurrentLocale.get(urlHelper.key("activate.error.invalidrange"))));
		}
		else if( request.getUntil().before(params.getDateNow()) )
		{
			errors
				.add(new ValidationError("fromuntil", CurrentLocale.get(urlHelper.key("activate.error.invalidtoday"))));
		}
	}

	public List<ActivateRequest> getActivatedRequests()
	{
		return requests;
	}

}
