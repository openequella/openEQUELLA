package com.tle.core.portal.service;

import java.util.Collection;
import java.util.Set;

import com.tle.common.portal.PortletTypeTarget;

/**
 * @author aholland
 */
interface PrivatePortletService extends PortletService
{
	// @SecureOnReturn(priv="CREATE_PORTLET")
	Set<PortletTypeTarget> filterContributableTypes(Collection<PortletTypeTarget> all);
}
