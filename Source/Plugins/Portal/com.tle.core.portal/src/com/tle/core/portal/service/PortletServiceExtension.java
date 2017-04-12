package com.tle.core.portal.service;

import java.util.List;

import com.dytech.edge.common.valuebean.ValidationError;
import com.tle.common.portal.entity.Portlet;

public interface PortletServiceExtension
{
	void doValidation(PortletEditingBean newPortlet, List<ValidationError> errors);

	void deleteExtra(Portlet portlet);

	void edit(Portlet oldPortlet, PortletEditingBean newPortlet);

	void add(Portlet portlet);

	void loadExtra(Portlet portlet);

	void changeUserId(String fromUserId, String toUserId);
}
