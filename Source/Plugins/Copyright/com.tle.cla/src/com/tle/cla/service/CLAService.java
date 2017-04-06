package com.tle.cla.service;

import com.tle.beans.cla.CLAHolding;
import com.tle.beans.cla.CLAPortion;
import com.tle.beans.cla.CLASection;
import com.tle.core.copyright.service.CopyrightService;

public interface CLAService extends CopyrightService<CLAHolding, CLAPortion, CLASection>
{

	void validateHolding(CLAHolding holding);

}
