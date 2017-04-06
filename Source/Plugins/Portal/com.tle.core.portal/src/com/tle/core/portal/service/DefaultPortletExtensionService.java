package com.tle.core.portal.service;

import java.util.List;

import javax.inject.Singleton;

import com.dytech.edge.common.valuebean.ValidationError;
import com.tle.common.portal.entity.Portlet;
import com.tle.core.guice.Bind;

@Bind
@Singleton
public class DefaultPortletExtensionService implements PortletServiceExtension
{
	@Override
	public void changeUserId(String fromUserId, String toUserId)
	{
		// Nothing to change
	}

	@Override
	public void deleteExtra(Portlet portlet)
	{
		// No
	}

	@Override
	public void edit(Portlet oldPortlet, PortletEditingBean newPortlet)
	{
		// No
	}

	@Override
	public void add(Portlet portlet)
	{
		// No
	}

	@Override
	public void loadExtra(Portlet portlet)
	{
		// No
	}

	@Override
	public void doValidation(PortletEditingBean newPortlet, List<ValidationError> errors)
	{
		// No
	}
}
