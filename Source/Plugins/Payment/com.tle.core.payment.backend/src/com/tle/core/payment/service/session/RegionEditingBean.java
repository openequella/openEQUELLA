package com.tle.core.payment.service.session;

import java.util.Set;

import com.google.common.collect.Sets;
import com.tle.core.services.entity.EntityEditingBean;

public class RegionEditingBean extends EntityEditingBean
{
	private static final long serialVersionUID = 1L;

	private final Set<String> countries = Sets.newHashSet();

	public Set<String> getCountries()
	{
		return countries;
	}
}