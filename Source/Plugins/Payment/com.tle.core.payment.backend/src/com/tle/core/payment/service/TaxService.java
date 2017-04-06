package com.tle.core.payment.service;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import com.tle.common.payment.entity.TaxType;
import com.tle.core.payment.service.session.TaxTypeEditingBean;
import com.tle.core.services.entity.AbstractEntityService;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public interface TaxService extends AbstractEntityService<TaxTypeEditingBean, TaxType>
{
	String ENTITY_TYPE = "TAX";
	String FIELD_CODE = "code";
	String FIELD_PERCENT = "percent";

	BigDecimal calculateTax(BigDecimal price, Currency currency, List<TaxType> taxes);
}
