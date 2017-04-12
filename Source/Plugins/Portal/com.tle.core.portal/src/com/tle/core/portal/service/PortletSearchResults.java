package com.tle.core.portal.service;

import java.util.List;

import com.tle.common.portal.entity.Portlet;
import com.tle.common.searching.SimpleSearchResults;

/**
 * @author aholland
 */
public class PortletSearchResults extends SimpleSearchResults<Portlet>
{
	private static final long serialVersionUID = 1L;

	protected PortletSearchResults(List<Portlet> results, int count, int offset, int available)
	{
		super(results, count, offset, available);
	}
}
