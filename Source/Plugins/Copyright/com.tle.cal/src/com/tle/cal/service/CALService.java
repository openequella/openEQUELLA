package com.tle.cal.service;

import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.cal.CALSection;
import com.tle.core.copyright.service.CopyrightService;

public interface CALService extends CopyrightService<CALHolding, CALPortion, CALSection>
{
	void validateHolding(CALHolding holding, boolean ignoreOverrides, boolean skipPercentage);

}
