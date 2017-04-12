package com.tle.web.payment.service;

import com.tle.core.payment.PaymentSettings;
import com.tle.web.sections.SectionInfo;

/**
 * @author Aaron
 */
public interface PaymentWebService
{
	PaymentSettings getSettings(SectionInfo info);

	void saveSettings(SectionInfo info, PaymentSettings settings);
}
