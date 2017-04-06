package com.tle.core.payment.service;

import java.util.Set;

import com.tle.common.payment.entity.Region;
import com.tle.core.payment.service.session.RegionEditingBean;
import com.tle.core.services.entity.AbstractEntityService;

@SuppressWarnings("nls")
public interface RegionService extends AbstractEntityService<RegionEditingBean, Region>
{
	String ENTITY_TYPE = "REGION";

	boolean containsCountry(Set<Region> regions, String country);
}
